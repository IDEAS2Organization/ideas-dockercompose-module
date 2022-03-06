try {
    function getNameForm() {
      return "<input id='name'/>\
      ";
    }
    operationId = operationStructure.id;
    var data = {};
    data.fileUri = fileUri;
    data.content = EditorManager.getEditorContentByUri(fileUri);
    data.id = operationId;
    data.username = principalUser;

    var fileUriSplited = fileUri.split("/");
    data.fileName = fileUriSplited[fileUriSplited.length - 1] 

  
    // Devuelve 'http://localhost:8081/ideas-dockerfile-language/language/operation/$opId/execute'
    operationUri =
      ModeManager.getBaseUri(
        ModeManager.calculateModelIdFromExt(
          ModeManager.calculateExtFromFileUri(fileUri)
        )
      ) + DEPRECATED_EXEC_OP_URI.replace("$opId", operationId);
  

    sendRequest(operationUri, data);
    OperationMetrics.play(operationId);

  } catch (error) {
    console.error(error);
  }
  