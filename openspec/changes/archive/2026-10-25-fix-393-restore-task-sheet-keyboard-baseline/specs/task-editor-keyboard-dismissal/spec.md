## REMOVED Requirements

### Requirement: Task editor keyboard dismissal settles in one attempt

**Reason**: Both implementations intended to provide this guarantee regressed normal title and
description focus handoffs in production.

**Migration**: Return to the pre-#229 behavior and leave issue #393 open for a future solution.

### Requirement: Task sheet remains stable while the keyboard closes

**Reason**: The gesture-routing and focus-clearing policy used to enforce this requirement caused the
keyboard to close and reopen during ordinary field switching.

**Migration**: Restore the standard Compose IME padding and sheet back-handler behavior from
`f311e02`.

### Requirement: Sheet dismissal follows keyboard dismissal

**Reason**: The rejected fixes coupled sheet dismissal registration to IME state and gesture-start
routing; that coupling is removed by the rollback.

**Migration**: Use the baseline sheet dismissal behavior until a replacement design is validated.

### Requirement: Adaptive task editor placement is unchanged

**Reason**: This requirement only described the failed keyboard-dismissal behavior across existing
placements and has no independent capability after that behavior is withdrawn.

**Migration**: Existing modal and docked placement specifications continue to govern layout.
