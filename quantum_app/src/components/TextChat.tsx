import React, { useState } from 'react';
import {
  IonContent,
  IonFooter,
  IonToolbar,
  IonTextarea,
  IonButton,
  IonList,
  IonItem,
  IonLabel,
} from '@ionic/react';

interface TextChatProps {
  contactName: string;
}

const TextChat: React.FC<TextChatProps> = ({ contactName }) => {
  const [messages, setMessages] = useState<string[]>([]);
  const [inputMessage, setInputMessage] = useState('');

  const handleMessageSend = () => {
    if (inputMessage.trim() !== '') {
      setMessages([...messages, inputMessage]);
      setInputMessage('');
    }
  };

  return (
    <>
      <IonContent>
        <IonList>
          {messages.map((message, index) => (
            <IonItem key={index}>
              <IonLabel>
                <h3>{contactName}</h3>
                <p>{message}</p>
              </IonLabel>
            </IonItem>
          ))}
        </IonList>
      </IonContent>
      <IonFooter>
        <IonToolbar>
          <IonTextarea
            placeholder="Escribe un mensaje..."
            value={inputMessage}
            onIonChange={(e) => setInputMessage(e.detail.value!)}
            autoGrow={true}
          ></IonTextarea>
          <IonButton fill="clear" onClick={handleMessageSend}>
            Enviar
          </IonButton>
        </IonToolbar>
      </IonFooter>
    </>
  );
};

export default TextChat;
