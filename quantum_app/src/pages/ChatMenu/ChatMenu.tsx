import React, { useState } from 'react';
import {
  IonContent,
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonMenuButton,
} from '@ionic/react';
import ChatList from '../../components/ChatList';
import { useHistory } from 'react-router-dom';

const ChatMenu: React.FC = () => {
  
  const [selectedChat, setSelectedChat] = useState<{ id: number; name: string } | null>(null);

  const chats = [
    { id: 1, name: 'Contacto 1' },
    { id: 2, name: 'Contacto 2' },
    { id: 3, name: 'Contacto 3' },
  ];

  const history = useHistory();

  const handleChatSelected = (chat: any) => {
    setSelectedChat(chat);
    history.push(`/chat/chat.tsx`);
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonMenuButton />
          </IonButtons>
          <IonTitle>Chats</IonTitle>
        </IonToolbar>
      </IonHeader>
        <IonContent>
          <ChatList chats={chats} onChatSelected={handleChatSelected} />
        </IonContent>
    </IonPage>
  );
};

export default ChatMenu;