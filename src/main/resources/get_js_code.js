// En language manifest, la operación build contiene esta función después de pasarla por https://jscompress.com/
// Luego se sustituyen las comillas dobles por simples para evitar problemas de sintaxis

function getCode(operationStructure, fileUri) {
  if (document.getElementById("dockercompose-module-data"))
    document.getElementById("dockercompose-module-data").remove();
  var s = document.createElement("script"); // Crea un elemento <script> en el documento
  s.type = "application/javascript";
  s.id = "dockercompose-module-data";
  // Se definen las variables de JS en el documento para que puedan ser usados después por el segundo script que se crea
  s.text =
    "var operationStructure = " +
    JSON.stringify(operationStructure) +
    ";var fileUri = '" +
    fileUri +
    "';";
  document.body.appendChild(s);

  if (document.getElementById("dockercompose-module-action"))
    document.getElementById("dockercompose-module-action").remove();

  var s1 = document.createElement("script");
  s1.type = "application/javascript";
  s1.id = "dockercompose-module-action";

  // Devuelve 'http://localhost:8081/ideas-dockerfile-language/language/operation/$opId/javascript'
  s1.src =
    ModeManager.getBaseUri(
      ModeManager.calculateModelIdFromExt(
        ModeManager.calculateExtFromFileUri(fileUri)
      )
    ) +
    "/language/operation/" +
    operationStructure.id +
    "/javascript";
  document.body.appendChild(s1);

  if (!document.getElementById("dockercompose-module-util")) {
    var s2 = document.createElement("script");
    s2.type = "application/javascript";
    s2.id = "dockercompose-module-util";
    s2.src =
      ModeManager.getBaseUri(
        ModeManager.calculateModelIdFromExt(
          ModeManager.calculateExtFromFileUri(fileUri)
        )
      ) + "/language/operation/util/javascript";
    document.body.appendChild(s2);
  }
}
