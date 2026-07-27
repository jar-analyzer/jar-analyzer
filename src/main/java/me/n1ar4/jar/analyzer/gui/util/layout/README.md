# Swing grid compatibility layer

This package contains the small grid-layout runtime needed by the Java-built
Swing forms. It is adapted from the JetBrains IntelliJ IDEA UI Designer forms
runtime 7.0.3, which is distributed under the Apache License 2.0.

The classes live in the project's own namespace and have no dependency on
IntelliJ IDEA, GUI Designer, `.form` files, or `forms_rt`. Keeping the original
grid sizing semantics allows the hand-written Swing component trees to retain
their former layout:

- inherited default gaps;
- minimum, preferred, and maximum constraint hints;
- `CAN_SHRINK`, `CAN_GROW`, and `WANT_GROW` priorities;
- row/column spans, indentation, anchors, and fill modes.

Upstream license: <https://www.apache.org/licenses/LICENSE-2.0>
