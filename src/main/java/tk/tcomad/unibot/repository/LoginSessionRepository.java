package tk.tcomad.unibot.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import tk.tcomad.unibot.entity.LoginSession;

public interface LoginSessionRepository extends CrudRepository<LoginSession, String> {
    LoginSession findLoginSessionBySub(String sub);
    Optional<LoginSession> findLoginSessionByChatId(String chatId);
}
