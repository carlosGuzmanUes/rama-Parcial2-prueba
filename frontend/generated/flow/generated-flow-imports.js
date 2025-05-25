import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '9d5c9c3549706afb7064682f255b68a996a540823ba2298e26749d34db4fa719') {
    pending.push(import('./chunks/chunk-9d5c9c3549706afb7064682f255b68a996a540823ba2298e26749d34db4fa719.js'));
  }
  if (key === 'd7ec4635d62f12d99abb1b1b9763dc65eff3b872607481de96e07688a911e71b') {
    pending.push(import('./chunks/chunk-d7ec4635d62f12d99abb1b1b9763dc65eff3b872607481de96e07688a911e71b.js'));
  }
  if (key === '164840db58f2873114272b78d79f4788ff177a30b8b410530b90a8a226edba07') {
    pending.push(import('./chunks/chunk-164840db58f2873114272b78d79f4788ff177a30b8b410530b90a8a226edba07.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;