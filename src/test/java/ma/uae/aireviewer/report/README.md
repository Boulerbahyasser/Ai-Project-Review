# Tests de `report`

Miroir de `src/main/java/ma/uae/aireviewer/report`. Un test par classe, dans le meme
package que la classe testee.

Consultez le README du package correspondant dans `src/main/java` pour savoir ce
que fait chaque classe, et `../README.md` pour les regles communes aux tests.

## Rappels

- Aucun appel reel a un modele : utilisez `fixtures/FakeLlmProvider`.
- Aucun conteneur Docker lance.
- Aucune ecriture hors de `@TempDir`.
- Nommez les tests par le comportement attendu, pas par la methode :
  `refuse_un_score_hors_bornes()` plutot que `testValidate()`.
