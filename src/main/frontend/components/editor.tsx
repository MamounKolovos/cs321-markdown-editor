import { Button } from "@vaadin/react-components";
import React, { useRef, useState, useEffect } from "react";
import { useSubscription, useStompClient } from "react-stomp-hooks";

enum Style {
	BOLD,
	ITALICS,
	STRIKETHROUGH,
	HIGHLIGHT,
	CODE_BLOCK
}

type StyleDef = {
	name: string;
	value: string;
}

const StyleDefs: Record<Style, StyleDef> = {
	[Style.BOLD]: {
		name: "Bold",
		value: "**"
	},
	[Style.ITALICS]: {
		name: "Italics",
		value: "*"
	},
	[Style.STRIKETHROUGH]: {
		name: "Strikethrough",
		value: "~~"
	},
	[Style.HIGHLIGHT]: {
		name: "Highlight",
		value: "=="
	},
	[Style.CODE_BLOCK]: {
		name: "Code Block",
		value: "```"
	},
}

const charLimit = 500;
export default function Editor() {
	const editorRef = useRef<HTMLTextAreaElement>(null);
	const [text, setText] = useState("");
	const stompClient = useStompClient();


	useSubscription("/broadcasts/test", (message) => {
		console.info("new broadcast: " + message.body);
		if (editorRef.current) {
			editorRef.current.value = message.body
		} else {
			console.warn("editorRef.current is NULL!");
		}
	});

	const editor = (
		<textarea
			ref={editorRef}
			className="text-editor"
			onChange={async (event) => {
				setText(event.target.value);
				sendTextToServer(event.target.value);
			}}
		/>
	)

	const sendTextToServer = (text: string) => {
		if (!stompClient) {
			console.warn('no stomp client');
			return;
		}

		const content: string = JSON.stringify({
			content: text
		});

		stompClient.publish({
			destination: '/app/test',
			body: content
		});
	}

	const wrapSelection = (style: Style) => () => {
		if (!editorRef.current) return;
		const editor = editorRef.current;

		const {selectionStart, selectionEnd} = editorRef.current;
		const selectedText = editor.value.slice(selectionStart, selectionEnd);
		const styleValue = StyleDefs[style].value;

		const newText = editor.value.slice(0, selectionStart) + styleValue + selectedText + styleValue + editor.value.slice(selectionEnd);
		editor.value = newText;

		editor.focus();
		editor.setSelectionRange(selectionStart + styleValue.length, selectionEnd + styleValue.length)
	}

	const createStyleButton = (style: Style) => {
		const name = StyleDefs[style].name;
		return (
			<Button
				key={name}
				onClick={wrapSelection(style)}
			>
				{name}
			</Button>
		)
	}

	const styleButtons = Array.from(Object.keys(StyleDefs), (style) => createStyleButton(style as unknown as Style));

    return (
		<React.Fragment>
			{editor}
			{styleButtons}
		</React.Fragment>
	)
}
