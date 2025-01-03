import {
  AppLayout,
  DrawerToggle,
  ProgressBar,
  SideNav,
  SideNavItem,
  TextArea,
  TextField,
} from "@vaadin/react-components";
import {
  createMenuItems,
  useViewConfig,
} from "@vaadin/hilla-file-router/runtime.js";
import { Suspense, useEffect, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { Signal, signal, effect } from "@vaadin/hilla-react-signals";
import Editor from "Frontend/components/editor";

import {
  StompSessionProvider,
  useStompClient,
  useSubscription,
  withStompClient,
  withSubscription,
} from "react-stomp-hooks";

const vaadin = window.Vaadin as {
  documentTitleSignal: Signal<string>;
};
vaadin.documentTitleSignal = signal("");
effect(() => {
  document.title = vaadin.documentTitleSignal.value;
});

export default function MainLayout() {
  const currentTitle = useViewConfig()?.title ?? "";
  const navigate = useNavigate();
  const location = useLocation();

  const [enabled, setEnabled] = useState(false);
  const WEBSOCKET_URL = "ws://" + window.location.host + "/create-ws-connection";

  // useEffect(() => {
  //   vaadin.documentTitleSignal.value = currentTitle;
  // })
  vaadin.documentTitleSignal.value = currentTitle;

  return (
    <StompSessionProvider url={WEBSOCKET_URL}>
      <Editor />
    </StompSessionProvider>
    //   <TextArea
    //         label=""
    //         maxlength={500}
    //         helperText={"skibidi?"}
    //         onValueChanged={(event) => {
    //           console.log("ballsack")
    //         }}
    //       />
  );

  //BELOW IS COMMENTED BOILERPLATE, JUST FOR REFERENCE NOTHING ELSE

  // return (
  //     <AppLayout primarySection="drawer">
  //       {/* <div slot="drawer" className="flex flex-col justify-between h-full p-m">
  //         <header className="flex flex-col gap-m">
  //           <h1 className="text-l m-0">{vaadin.documentTitleSignal}</h1>
  //           <SideNav
  //               onNavigate={({path}) => navigate(path!)}
  //               location={location}
  //           >
  //             {createMenuItems().map(({to, title}) => (
  //                 <SideNavItem path={to} key={to}>
  //                   {title}
  //                 </SideNavItem>
  //             ))}
  //           </SideNav>
  //         </header>
  //       </div> */}

  //       {/* <DrawerToggle slot="navbar" aria-label="Menu toggle"></DrawerToggle>
  //       <h2 slot="navbar" className="text-l m-0">
  //         {vaadin.documentTitleSignal}
  //       </h2> */}

  //       {/* <Suspense fallback={<ProgressBar indeterminate className="m-0"/>}>
  //         <section className="view">
  //           <Outlet/>
  //         </section>
  //       </Suspense> */}
  //     </AppLayout>
  // );
}
