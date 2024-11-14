import { Button } from "@vaadin/react-components";
import React, { useRef, useState, useEffect } from "react";
import { useSubscription, useStompClient } from "react-stomp-hooks";

import { PresenceManager } from "Frontend/generated/endpoints"

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
    const [isSidebarOpen, setSidebarOpen] = useState(false);
    const [currentPage, setCurrentPage] = useState('main');
	const [text, setText] = useState("");
	const [html, setHtml] = useState("");
	const [userId, setUserId] = useState<number | null>(null);
	const stompClient = useStompClient();


	useSubscription("/broadcasts/updates", (message) => {
		const data = JSON.parse(message.body);
		if (editorRef.current) {
			editorRef.current.value = data.original.content
			setHtml(data.html);
		} else {
			console.warn("editorRef.current is NULL!");
		}
	});

	useEffect(() => {
		if (userId !== null) {
			return;
		}

		// request a user id from the server
		const getUserId = async () => {
			const response = await PresenceManager.generateUserId();
			setUserId(response);
		}

		getUserId();

		// request the current text and then update the text box accordingly
	}, []);

	const editor = (
		<textarea
			ref={editorRef}
			className="text-editor"
			onChange={async (event) => {
				setText(event.target.value);
				sendTextToServer(event.target.value);
			}}
		/>
	);

	const htmlView = (
		<div className="html-render" dangerouslySetInnerHTML={{__html: html}} />
	)

	const sendTextToServer = (text: string) => {
		if (!stompClient) {
			console.warn('no stomp client');
			return;
		}

		const content: string = JSON.stringify({
			content: text,
			senderId: userId
		});

		stompClient.publish({
			destination: '/app/update',
			body: content
		});
	}

    const wrapSelection = (style: Style) => () => {
        if (!editorRef.current) return;
        const editor = editorRef.current;

        const { selectionStart, selectionEnd } = editorRef.current;
        const selectedText = editor.value.slice(selectionStart, selectionEnd);
        const styleValue = StyleDefs[style].value;

        const newText = editor.value.slice(0, selectionStart) + styleValue + selectedText + styleValue + editor.value.slice(selectionEnd);
        editor.value = newText;

        editor.focus();
        editor.setSelectionRange(selectionStart + styleValue.length, selectionEnd + styleValue.length);
    }

    const createStyleButton = (style: Style) => {
        const name = StyleDefs[style].name;
        return (
            <Button
                key={name}
                onClick={wrapSelection(style)}
                className="style-button"
            >
                {name}
            </Button>
        );
    }

    const styleButtons = Array.from(Object.keys(StyleDefs), (style) => createStyleButton(style as unknown as Style));

    // Keyboard shortcut handling
    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (!editorRef.current) return;

            // Check if the editor is focused
            if (document.activeElement !== editorRef.current) return;

            switch (event.key) {
                case 'b': // Bold
                    if (event.ctrlKey || event.metaKey) {
                        event.preventDefault();
                        wrapSelection(Style.BOLD)();
                    }
                    break;
                case 'i': // Italics
                    if (event.ctrlKey || event.metaKey) {
                        event.preventDefault();
                        wrapSelection(Style.ITALICS)();
                    }
                    break;
                case 'u': // Strikethrough
                    if (event.ctrlKey || event.metaKey) {
                        event.preventDefault();
                        wrapSelection(Style.STRIKETHROUGH)();
                    }
                    break;
                case 'h': // Highlight
                    if (event.ctrlKey || event.metaKey) {
                        event.preventDefault();
                        wrapSelection(Style.HIGHLIGHT)();
                    }
                    break;
                case '\'': // Code Block
                    if (event.ctrlKey || event.metaKey) {
                        event.preventDefault();
                        wrapSelection(Style.CODE_BLOCK)();
                    }
                    break;
                default:
                    break;
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        return () => {
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, []);

    const renderMainSidebar = () => (
        <div className="sidebar-content">
            <p>
                <Button onClick={() => setCurrentPage('page1')} className="sidebar-page-button">
                    Shortcuts
                </Button>
            </p>
            <p>
                <Button onClick={() => setCurrentPage('page2')} className="sidebar-page-button">
                    Reference
                </Button>
            </p>
        </div>
    );

    const renderPage1 = () => (
        <div className="sidebar-content">
            <p><b>Bold</b></p>
            <p>ctrl + b</p>
            <p>-------------------------------</p>
            <p><b>Italics</b></p>
            <p>ctrl + i</p>
            <p>-------------------------------</p>
            <p><b>Strikethrough</b></p>
            <p>ctrl + u</p>
            <p>-------------------------------</p>
            <p><b>Highlight</b></p>
            <p>ctrl + h</p>
            <p>-------------------------------</p>
            <p><b>Code Block</b></p>
            <p>ctrl + '</p>
        </div>
    );

    const renderPage2 = () => (
        <div className="sidebar-content">
            <p><b>**Bold**</b></p>
            <p>-------------------------------</p>
            <p><i>*Italics*</i></p>
            <p>-------------------------------</p>
            <p><s>~~Strikethrough~~</s></p>
            <p>-------------------------------</p>
            <p><mark>==Highlight==</mark></p>
            <p>-------------------------------</p>
            <p><code>'''Code Block'''</code></p>
        </div>
    );

    const renderSidebarContent = () => {
        switch (currentPage) {
            case 'page1':
                return renderPage1();
            case 'page2':
                return renderPage2();
            default:
                return renderMainSidebar();
        }
    };

    return (
        <React.Fragment>
            <div className="header-bar">
                <div className="button-container">
                    {styleButtons}
                    <Button onClick={() => setSidebarOpen(!isSidebarOpen)} className="sidebar-button">
                        Menu
                    </Button>
                </div>
            </div>
			<div className="main-view">
				{editor}
				{htmlView}
			</div>
            {isSidebarOpen && (
                <div className="sidebar">
                    <div className="sidebar-header">
                        {currentPage === 'main' ? (
                            <h2 className="sidebar-title">Menu</h2>
                        ) : (
                            <Button onClick={() => setCurrentPage('main')} className="back-button">
                                Back
                            </Button>
                        )}
                        <Button onClick={() => setSidebarOpen(false)} className="close-sidebar-button">
                            Close
                        </Button>
                    </div>
                    {renderSidebarContent()}
                </div>
            )}
        </React.Fragment>
    )
}

