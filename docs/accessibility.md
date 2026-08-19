# Accessibility

LibraCore targets practical WCAG-oriented accessibility for its responsive web application.

## Required behavior

- Semantic page landmarks and heading order.
- Keyboard access to navigation, forms, tables, dialogs/panels, and operational actions.
- Strong visible focus indicators.
- Explicit form labels and useful validation/error messages.
- Status communication that does not rely on color alone.
- Touch targets usable on mobile/tablet layouts.
- Responsive reflow under browser zoom and larger text.
- Reduced-motion preference respected for non-essential animation.
- Meaningful loading/error/empty/success announcements where state changes dynamically.
- Decorative images use empty alternative text; informative images require useful alternatives.

## Manual release check

For each primary role (administrator, librarian, member):

1. Navigate sign-in and primary screens using keyboard only.
2. Confirm focus never becomes invisible or trapped.
3. Test at 200% zoom and narrow viewport.
4. Enable `prefers-reduced-motion` and confirm essential meaning is preserved.
5. Review form labels, error association, table headers, landmarks, and link/button names with a screen reader.
6. Verify active/inactive/error/success status is understandable without color.
7. Confirm mobile navigation remains reachable and does not obscure content.

## Automated checks

Automated accessibility scanners should be added to browser-level E2E tests. They supplement rather than replace manual review because keyboard flow, language clarity, and many screen-reader issues require human judgment.

## Reporting accessibility bugs

Open a normal issue unless the report contains sensitive personal/security information. Include browser/assistive technology, viewport/zoom settings, exact route/action, and expected versus actual behavior.
