import { TextArea } from "@vaadin/react-components";
import { useState } from "react";


const charLimit = 500;
export default function Editor() {
    const [text, setText] = useState("");

    return (
        <TextArea
            className="text-editor"
              label=""
              maxlength={charLimit}
              helperText={`${text.length}/${charLimit}`}
              value={text}
              onValueChanged={(event) => {
                setText(event.detail.value);
              }}
            />
      )
}