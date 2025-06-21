import '@vaadin/tooltip/src/vaadin-tooltip.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/button/src/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'd7ec4635d62f12d99abb1b1b9763dc65eff3b872607481de96e07688a911e71b') {
    pending.push(import('./chunks/chunk-4e84c0dd41c912553be0261f217d80678981a33413bba4326e72841706a4b5a8.js'));
  }
  if (key === '164840db58f2873114272b78d79f4788ff177a30b8b410530b90a8a226edba07') {
    pending.push(import('./chunks/chunk-14720d17a87493f2b9941e200f4e5afab4c05f2b7d79cffa29b49cbc3b4c620a.js'));
  }
  if (key === 'ae96b7ae897a7f6d5eec34eb46b7bd0396a2ff2d0a9f76df61a09d4a9cbfe842') {
    pending.push(import('./chunks/chunk-8a1319faad22759b9408bc6ae3361b7598666e90301da156dd25d043d850882f.js'));
  }
  if (key === '9d5c9c3549706afb7064682f255b68a996a540823ba2298e26749d34db4fa719') {
    pending.push(import('./chunks/chunk-4e84c0dd41c912553be0261f217d80678981a33413bba4326e72841706a4b5a8.js'));
  }
  if (key === 'e0c23605e363de17d9efc2cbdd51009c3031da58e00e1e75bfeb7bf27572c0d4') {
    pending.push(import('./chunks/chunk-dbbbbdfafd4536f63ad60fed106b1cff28e5c7c230e9b2cf2409f8898904e0fb.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;