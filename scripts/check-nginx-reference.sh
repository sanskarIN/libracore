#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source_config="${repo_root}/deploy/nginx/libracore.conf"

if ! command -v nginx >/dev/null 2>&1; then
  echo "nginx is required to validate ${source_config}." >&2
  exit 2
fi
if ! command -v openssl >/dev/null 2>&1; then
  echo "openssl is required to create a temporary validation certificate." >&2
  exit 2
fi
if ! command -v sed >/dev/null 2>&1; then
  echo "sed is required to prepare the isolated validation configuration." >&2
  exit 2
fi

workdir="$(mktemp -d)"
cleanup() {
  rm -rf "${workdir}"
}
trap cleanup EXIT

mkdir -p "${workdir}/frontend" "${workdir}/logs"
printf '<!doctype html><title>LibraCore nginx validation</title>\n' > "${workdir}/frontend/index.html"

openssl req \
  -x509 \
  -newkey rsa:2048 \
  -nodes \
  -days 1 \
  -subj '/CN=library.example.org' \
  -keyout "${workdir}/key.pem" \
  -out "${workdir}/cert.pem" \
  >/dev/null 2>&1

mime_types="$(nginx -V 2>&1 | sed -n 's/.*--conf-path=\([^ ]*\).*/\1/p')"
if [[ -n "${mime_types}" ]]; then
  mime_types="$(dirname "${mime_types}")/mime.types"
fi
if [[ -z "${mime_types}" || ! -f "${mime_types}" ]]; then
  for candidate in /etc/nginx/mime.types /usr/local/nginx/conf/mime.types; do
    if [[ -f "${candidate}" ]]; then
      mime_types="${candidate}"
      break
    fi
  done
fi
if [[ -z "${mime_types}" || ! -f "${mime_types}" ]]; then
  echo "Unable to locate nginx mime.types." >&2
  exit 2
fi

escaped_workdir="${workdir//\//\\/}"
escaped_mime_types="${mime_types//\//\\/}"

sed \
  -e "s@include       mime.types;@include       ${escaped_mime_types};@" \
  -e "s@pid /run/nginx.pid;@pid ${escaped_workdir}\\/nginx.pid;@" \
  -e "s@listen 80;@listen 18080;@" \
  -e "s@listen 443 ssl;@listen 18443 ssl;@" \
  -e "s@/etc/letsencrypt/live/library.example.org/fullchain.pem@${escaped_workdir}\\/cert.pem@" \
  -e "s@/etc/letsencrypt/live/library.example.org/privkey.pem@${escaped_workdir}\\/key.pem@" \
  -e "s@root /srv/libracore/frontend;@root ${escaped_workdir}\\/frontend;@" \
  "${source_config}" > "${workdir}/nginx.conf"

nginx -t -c "${workdir}/nginx.conf" -p "${workdir}/"
echo "Nginx reference configuration syntax is valid."
