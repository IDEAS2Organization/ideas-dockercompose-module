try {

  function getSelectFlagsForm(data) {
    var res =
      "<fieldset>\
    <legend>Please select one of the following</legend>$content</fieldset>";
    var option =
      '<input name="flagsNames" type="checkbox" id="$value" value="$value"/> <label for="$value">$name</label></br>';

    var allFlags = {
      "--force-recreate": "Recreate container even if config. and image haven't changed.",
      "--no-recreate": "Don't recreate containers that already exists.",
      "--remove-orphans": "Remove containers not defined in the Compose file."
    }

    var content = "";
    for (var flag in allFlags) { // Crea un input type checkbox por flag posible
      content += option
        .replaceAll("$value", flag)
        .replaceAll("$name", flag + ":" + allFlags[flag]);
    }
    res = res.replace("$content", content);
    return res;
  }

  operationId = operationStructure.id;
  var data = {};
  data.fileUri = fileUri;
  data.content = EditorManager.getEditorContentByUri(fileUri);
  data.id = operationId;
  data.username = principalUser;

  var fileUriSplited = fileUri.split("/");
  data.fileName = fileUriSplited[fileUriSplited.length - 1] 

  // Devuelve 'http://localhost:8081/ideas-dockercompose-language/language/operation/$opId/execute'
  operationUri =
    ModeManager.getBaseUri(
      ModeManager.calculateModelIdFromExt(
        ModeManager.calculateExtFromFileUri(fileUri)
      )
    ) + DEPRECATED_EXEC_OP_URI.replace("$operationId", operationId);

  var form = getSelectFlagsForm();

  showModal(
    "Docker-Compose Up",
    form,
    "Execute",
    function () {
      var selected = $("[name='flagsNames']");
      var res = "";
      for (var i = 0; i < selected.length; i++) {
        if (selected[i].checked) {
          res += selected[i].value + " ";
        }
      }
      data.flags = res;
      OperationMetrics.play(operationId);
      sendRequest(operationUri, data);
      closeModal();
    },
    closeModal,
    ""
  );

} catch (error) {
  console.error(error);
}
