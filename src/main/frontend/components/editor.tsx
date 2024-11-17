import { Button } from "@vaadin/react-components";
import React, { useRef, useState, useEffect } from "react";
import { useSubscription, useStompClient, IMessage, StompHeaders } from "react-stomp-hooks";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBold, faItalic, faStrikethrough, faHighlighter, faCode, faTimes, faArrowLeft, faBars } from '@fortawesome/free-solid-svg-icons';

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
    icon: React.ReactNode;
}

const StyleDefs: Record<Style, StyleDef> = {
    [Style.BOLD]: {
        name: "Bold",
        value: "**",
        icon: <FontAwesomeIcon icon={faBold} />
    },
    [Style.ITALICS]: {
        name: "Italics",
        value: "*",
        icon: <FontAwesomeIcon icon={faItalic} />
    },
    [Style.STRIKETHROUGH]: {
        name: "Strikethrough",
        value: "~~",
        icon: <FontAwesomeIcon icon={faStrikethrough} />
    },
    [Style.HIGHLIGHT]: {
        name: "Highlight",
        value: "==",
        icon: <FontAwesomeIcon icon={faHighlighter} />
    },
    [Style.CODE_BLOCK]: {
        name: "Code Block",
        value: "```",
        icon: <FontAwesomeIcon icon={faCode} />
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
	}, []);

	useEffect(() => {
		if (userId === null) {
			return;
		}

		const getInitialText = async (userId: number) => {
			if (!editorRef.current) {
				console.error("editor reference is NULL!!");
				return;
			}

			const response = await PresenceManager.getInitialText(userId);

			if (!response || !response.original || !response.html) {	// guard against invalid response
				console.error("Unable to get initial text + parser response from PresenceManager!!");
				return;
			}

			setText(response.original.content!);
			setHtml(response.html);

			editorRef.current.value = response.original.content || "";
		}

		getInitialText(userId);
	}, [userId]);	// only run this when userId gets set... should be on the first connect

	const editor = (
		<textarea
			ref={editorRef}
			className="text-editor"
            spellCheck={true}
            placeholder="TYPE HERE!"
			onChange={async (event) => {
                handleTextChange(event.target.value);
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

        setText(newText);
        handleTextChange(newText);
        sendTextToServer(newText);

        editor.focus();
        editor.setSelectionRange(selectionStart + styleValue.length, selectionEnd + styleValue.length);
    }

    const createStyleButton = (style: Style) => {
        const { name, icon } = StyleDefs[style];
        return (
            <Button
                key={name}
                onClick={wrapSelection(style)}
                className="style-button"
            >
                {icon}
            </Button>
        );
    }

    const styleButtons = Array.from(Object.keys(StyleDefs), (style) => createStyleButton(style as unknown as Style));


    // keyboard shortcut handling
    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (!editorRef.current) return;

            // check if the editor is focused
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
            <p><h1>#Header1</h1></p>
            <p><h2>##Header2</h2></p>
            <p><h3>###Header3</h3></p>
            <p><h4>####Header4</h4></p>
            <p><h5>#####Header5</h5></p>
            <p><h6>######Header6</h6></p>
            <p>-------------------------------</p>
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

    // Footer State and Functions
    const [charCount, setCharCount] = useState(0);
    const [wordCount, setWordCount] = useState(0);
    const [lineCount, setLineCount] = useState(0);
    const [cursorLine, setCursorLine] = useState(1);
    const [cursorColumn, setCursorColumn] = useState(1);

    const updateStats = (newText: string) => {
        setCharCount(newText.length);
        setWordCount(newText.trim().split(/\s+/).filter(word => word.length > 0).length);
        setLineCount(newText.split("\n").length);
    };

    const handleTextChange = (newText: string) => {
        setText(newText);
        updateStats(newText);
        handleCursorMove();
    };

    const handleCursorMove = () => {
        if (editorRef.current) {
            const text = editorRef.current.value;
            const cursorIndex = editorRef.current.selectionStart;
    
            const lines = text.substring(0, cursorIndex).split("\n");
            setCursorLine(lines.length);
    
            const columnNumber = lines[lines.length - 1].length + 1;
            setCursorColumn(columnNumber);
        }
    };

    useEffect(() => {
        const updateCursorPosition = () => {
            handleCursorMove();
        };
    
        const editorElement = editorRef.current;
        if (editorElement) {
            editorElement.addEventListener('keyup', updateCursorPosition);
            editorElement.addEventListener('click', updateCursorPosition);
        }
    
        return () => {
            if (editorElement) {
                editorElement.removeEventListener('keyup', updateCursorPosition);
                editorElement.removeEventListener('click', updateCursorPosition);
            }
        };
    }, []);

    const clearEditor = () => {
        if (editorRef.current) {
            editorRef.current.value = ''; // clear the text area
        }
        setText('');  // reset the text state
        setHtml('');  // reset the HTML state
    };


    return (
        <React.Fragment>
            <div className="header-bar">
                <div className="button-container">
                    {styleButtons}
                </div>
                <div className = "sidebar-button-container">
                    <Button onClick={clearEditor} className="clear-button">
                        <b>Clear</b>
                    </Button>
                    <Button onClick={() => setSidebarOpen(!isSidebarOpen)} className="sidebar-button">
                        <FontAwesomeIcon icon={faBars} />
                    </Button>
                </div>
            </div>
			<div className="main-view">
                <footer className="editor-footer">
                    <span>Characters: {charCount}</span>
                    <span>Words: {wordCount}</span>
                    <span>Lines: {lineCount}</span>
                    <span>Cursor: Ln {cursorLine}, Col {cursorColumn}</span>
                </footer>

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
                                <FontAwesomeIcon icon={faArrowLeft} />
                            </Button>
                        )}
                        <Button onClick={() => setSidebarOpen(false)} className="close-sidebar-button">
                            <FontAwesomeIcon icon={faTimes} />
                        </Button>
                    </div>
                    {renderSidebarContent()}
                </div>
            )}
        </React.Fragment>
    )
}

