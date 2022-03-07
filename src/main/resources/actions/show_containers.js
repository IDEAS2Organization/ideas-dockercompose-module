try {

    function getSelectFlagsForm(data) {
      var res =
        "<fieldset>\
      <legend>Please select one of the following</legend>$content</fieldset>";
      var option =
        '<input name="flagsNames" type="checkbox" id="$value" value="$value"/> <label for="$value">$name</label></br>';

      var allFlags = ["--all", "--latest", "--no-trunc", "--quiet", "--size" ];

      var content = "";
      for (var i in allFlags) { // Crea un input type checkbox por flag posible
        content += option
          .replaceAll("$value", allFlags[i])
          .replaceAll("$name", allFlags[i]);
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
  
    // Devuelve 'http://localhost:8081/ideas-dockerfile-language/language/operation/$opId/execute'
    operationUri =
      ModeManager.getBaseUri(
        ModeManager.calculateModelIdFromExt(
          ModeManager.calculateExtFromFileUri(fileUri)
        )
      ) + DEPRECATED_EXEC_OP_URI.replace("$operationId", operationId);
  
    var form = getSelectFlagsForm();

    showModal(
      "Show containers",
      form,
      "Show",
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
  