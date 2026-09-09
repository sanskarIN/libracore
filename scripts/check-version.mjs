import { readFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

// Temporary release-verification branch marker; no runtime behavior changes.
const scriptDirectory = dirname(fileURLToPath(import.meta.url))
const repositoryRoot = resolve(scriptDirectory, '..')

const [packageText, lockText, pomText] = await Promise.all([
  readFile(resolve(repositoryRoot, 'frontend/package.json'), 'utf8'),
  readFile(resolve(repositoryRoot, 'frontend/package-lock.json'), 'utf8'),
  readFile(resolve(repositoryRoot, 'backend/pom.xml'), 'utf8'),
])

const frontendVersion = JSON.parse(packageText).version
const lockfile = JSON.parse(lockText)
const lockfileVersion = lockfile.version
const rootPackageVersion = lockfile.packages?.['']?.version
const projectVersionMatch = pomText.match(/<artifactId>libracore-backend<\/artifactId>\s*<version>([^<]+)<\/version>/)

if (!projectVersionMatch) {
  console.error('Could not determine backend project version from backend/pom.xml.')
  process.exit(1)
}

const backendVersion = projectVersionMatch[1]
const expectedVersion = process.argv[2]?.replace(/^v/, '')

if (frontendVersion !== backendVersion) {
  console.error(`Version mismatch: frontend=${frontendVersion}, backend=${backendVersion}`)
  process.exit(1)
}

if (lockfileVersion !== frontendVersion || rootPackageVersion !== frontendVersion) {
  console.error(
    `Version mismatch: package=${frontendVersion}, lockfile=${lockfileVersion}, lockfileRoot=${rootPackageVersion}`,
  )
  process.exit(1)
}

if (expectedVersion && frontendVersion !== expectedVersion) {
  console.error(`Version mismatch: manifests=${frontendVersion}, expected=${expectedVersion}`)
  process.exit(1)
}

console.log(`LibraCore version ${frontendVersion} is synchronized across frontend, backend, and lockfile manifests.`)
