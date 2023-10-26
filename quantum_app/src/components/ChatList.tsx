import React from 'react';
import { IonList, IonItem, IonLabel } from '@ionic/react';

interface Chat {
  id: number;
  name: string;
}

interface ChatListProps {
  chats: Chat[];
  onChatSelected: (chat: Chat) => void;
}

const ChatList: React.FC<ChatListProps> = ({ chats, onChatSelected }) => {
  return (
    <IonList>
      {chats.map((chat) => (
        <IonItem key={chat.id} button onClick={() => onChatSelected(chat)}>
          <IonLabel>{chat.name}</IonLabel>
        </IonItem>
      ))}
    </IonList>
  );
};

export default ChatList;
