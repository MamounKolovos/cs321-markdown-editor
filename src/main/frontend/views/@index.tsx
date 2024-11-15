import { Button, Notification, TextField } from "@vaadin/react-components";
import { HelloEndpoint } from "Frontend/generated/endpoints.js";
import { useState } from "react";
import type { ViewConfig } from "@vaadin/hilla-file-router/types.js";

export const config: ViewConfig[] = [

];

export default function DudesView() {
  const [name, setName] = useState("");

  return (
    <>
      <TextField
        label="This is Dude"
        onValueChanged={(e) => {
          setName(e.detail.value);
        }}
      />
      <Button
        onClick={async () => {
          const serverResponse = await HelloEndpoint.sayHello(name);
          Notification.show(serverResponse);
        }}
      >
        Say hello
      </Button>
    </>
  );
}

