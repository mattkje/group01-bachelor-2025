package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class WarehouseWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Connection established with session: " + session.getId());
        session.sendMessage(new TextMessage("Connection established"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Connection closed with session: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Message received from session " + session.getId() + ": " + message.getPayload());
        // Handle incoming messages if needed
    }

    public void sendMessageToAll(String message) throws IOException {
        System.out.println("sendMessageToAll called with message: " + message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                System.out.println("Sending message to session: " + session.getId());
                session.sendMessage(new TextMessage(message));
                System.out.println("Message sent to session: " + session.getId());
            } else {
                System.out.println("Session is not open: " + session.getId());
            }
        }
    }

    public int getConnectedClients() {
        return sessions.size();
    }
}