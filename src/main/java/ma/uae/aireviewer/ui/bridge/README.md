# `ui.bridge` — Passerelle vers le thread JavaFX

## Contenu

| Classe | Role |
|---|---|
| `FxAnalysisListener` | Rebascule les evenements du moteur vers le thread JavaFX |

## Le probleme resolu

JavaFX impose que toute modification de l'interface se fasse sur son thread
d'application. Or `AnalysisEventPublisher` diffuse les evenements depuis le thread
qui execute l'analyse.

Un abonne qui toucherait directement un composant JavaFX declencherait une
`IllegalStateException: Not on FX application thread` — ou, pire, un affichage
corrompu de facon intermittente et difficile a reproduire.

## La solution, en une classe

```java
public void onEvent(AnalysisEvent event) {
    Platform.runLater(() -> delegate.onEvent(event));
}
```

C'est un **Decorator** : meme interface, meme comportement, avec le changement de
thread ajoute autour.

## Pourquoi une classe dediee plutot qu'un `runLater` dans chaque vue

Un seul endroit garantit la regle. Disperser les `Platform.runLater` mene
inevitablement a un oubli, et le bug qui en resulte n'apparait qu'une fois sur
dix — au pire moment, souvent pendant la demonstration.

## Regle

C'est le **seul** point de contact entre le monde du moteur et celui de JavaFX.
Aucune autre classe de `ui` ne doit appeler `Platform.runLater`.
