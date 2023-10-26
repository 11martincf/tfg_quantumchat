import React, { useState } from 'react';
import { IonContent, IonPage, IonButton, IonButtons, IonMenuButton, IonHeader, IonToolbar, IonTitle } from '@ionic/react';

const Home: React.FC = () => {
  const [showTutorialFirst, setShowTutorialFirst] = useState(true);
  const [showButton, setShowButton] = useState(true);

  const tutorialContent = (
    <>
      {/* Tutorial */}
      <h1>Bienvenid@ a Quantum Chat</h1>
      <p>Esta aplicación te permite obtener contraseñas y chatear con tus contactos.</p>
      <h2>¿Cómo funciona?</h2>
      <p>Lorem ipsum...</p>
      <div className="ion-text-center">
        {showButton && <IonButton onClick={() => { setShowTutorialFirst(false); setShowButton(false); }}>Entendido</IonButton>}
      </div>
    </>
  );

  const widgetsContent = (
    <>
      {/* Widgets */}
      <h3>Claves en proceso:</h3>
      <p>Widgets con barra de progreso de las claves en proceso de obtención</p>
    </>
  );

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonMenuButton />
          </IonButtons>
          <IonTitle>Inicio</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">
        {showTutorialFirst ? (
          <>
            {tutorialContent}
            {widgetsContent}
          </>
        ) : (
          <>
            {widgetsContent}
            {tutorialContent}
          </>
        )}
      </IonContent>
    </IonPage>
  );
};

export default Home;

