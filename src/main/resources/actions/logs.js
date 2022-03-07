async function del() {
    try {
      function parseContainersOutput(htmlMessage) {
        var result = {};
        var rows = htmlMessage.split("\n");
        for (var row in rows) {
          if (!rows[row].includes("<")) {
            row_str = rows[row].split(" ");
            if (row_str[0] !== "") result[row_str[0]] = rows[row];
          }
        }
        return result;
      }
      async function getSelectForm(data) {
        var res =
          "<fieldset>\
      <legend>Please select one of the following</legend>$content</fieldset>";
        var option =
          '<input name="containerNames" type="radio" id="$value" value="$value"/> <label for="$value">$name</label></br>';
        var uri =
          ModeManager.getBaseUri(
            ModeManager.calculateModelIdFromExt(
              ModeManager.calculateExtFromFileUri(fileUri)
            )
          ) + DEPRECATED_EXEC_OP_URI.replace("$opId", "show_containers");
  
        let tmp_data = {
          ...data,
        };
  
        tmp_data.id = "show_containers";
        tmp_data.flags = "--all";
        var result = await $.ajax({
          url: uri,
          type: "POST",
          data: tmp_data,
        });
  
        var containers = parseContainersOutput(result.htmlMessage);
  
        var content = "";
        for (var container in containers) {
          content += option
            .replaceAll("$value", container)
            .replaceAll("$name",containers[container].split(/\s+/)[1] + " - " + container); // split en función de cualquier espacio, devuelve el nombre del contenedor
        }
        res = res.replace("$content", content);
        return res;
      }
      operationId = operationStructure.id;
      var data = {};
      data.id = operationId;
      data.username = principalUser;
  
      // Devuelve 'http://localhost:8081/ideas-dockercompose-language/language/operation/$opId/execute'
      operationUri =
        ModeManager.getBaseUri(
          ModeManager.calculateModelIdFromExt(
            ModeManager.calculateExtFromFileUri(fileUri)
          )
        ) + DEPRECATED_EXEC_OP_URI.replace("$opId", operationId);
  
      OperationMetrics.play(operationId);
      var form = await getSelectForm(data);
      OperationMetrics.stop();
  
      showModal(
        "Get logs from container",
        form,
        "Get logs",
        function () {
          var selected = $("[name='containerNames']");
          var res = "";
          for (var i = 0; i < selected.length; i++) {
            if (selected[i].checked) {
              res += selected[i].value + " ";
            }
          }
          data.containerId = res;
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
  }
  del();
  