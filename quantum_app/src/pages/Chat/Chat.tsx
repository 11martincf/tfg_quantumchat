import React from 'react';
import { useParams } from 'react-router-dom';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonMenuButton,
} from '@ionic/react';
import TextChat from '../../components/TextChat';


const Chat: React.FC = () => {
  // Recuperamos el id del chat de los parámetros de la ruta
  const { id } = useParams<{ id: string }>();
  
  // Aquí puedes hacer lógica adicional para cargar los mensajes del chat por ejemplo
  // usando el id del chat recuperado
  
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonMenuButton />
          </IonButtons>
          <IonTitle>{id}</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent>
        <TextChat contactName={id} />
      </IonContent>
    </IonPage>
  );
};

export default Chat;
