import { IonApp, IonRouterOutlet, IonSplitPane, IonMenu, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonMenuToggle, setupIonicReact } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import { Redirect, Route } from 'react-router-dom';
import Home from './pages/Home/Home';
import Passwords from './pages/Passwords/Passwords';
import ChatMenu from './pages/ChatMenu/ChatMenu';
import Chat from './pages/Chat/Chat';

/* Core CSS required for Ionic components to work properly */
import '@ionic/react/css/core.css';

/* Basic CSS for apps built with Ionic */
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

/* Theme variables */
import './theme/variables.css';

setupIonicReact();

const App: React.FC = () => (
  <IonApp>
    <IonReactRouter>
      <IonSplitPane contentId="main">
        <IonMenu contentId="main" type="overlay">
          <IonHeader>
            <IonToolbar>
              <IonTitle>Menú</IonTitle>
            </IonToolbar>
          </IonHeader>
          <IonContent>
            <IonList>
              <IonMenuToggle key="home" autoHide={false}>
                <IonItem button routerLink="/home" routerDirection="none">
                  <IonLabel>Inicio</IonLabel>
                </IonItem>
              </IonMenuToggle>
              <IonMenuToggle key="passwords" autoHide={false}>
                <IonItem button routerLink="/passwords" routerDirection="none">
                  <IonLabel>Contraseñas</IonLabel>
                </IonItem>
              </IonMenuToggle>
              <IonMenuToggle key="chatmenu" autoHide={false}>
                <IonItem button routerLink="/chatmenu" routerDirection="none">
                  <IonLabel>Chats</IonLabel>
                </IonItem>
              </IonMenuToggle>
            </IonList>
          </IonContent>
        </IonMenu>
        <IonRouterOutlet id="main">
          <Route path="/home" component={Home} exact />
          <Route path="/passwords" component={Passwords} exact />
          <Route path="/chatmenu" component={ChatMenu} exact />
          <Route path="/chat/:id" component={Chat} exact />
          <Redirect exact from="/" to="/home" />
        </IonRouterOutlet>
      </IonSplitPane>
    </IonReactRouter>
  </IonApp>
);

export default App;
