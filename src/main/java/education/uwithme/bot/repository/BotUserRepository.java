package education.uwithme.bot.repository;

import java.util.Optional;

import education.uwithme.bot.entity.BotUser;
import org.springframework.data.repository.CrudRepository;

public interface BotUserRepository extends CrudRepository<BotUser, Long> {
    Optional<BotUser> findBotUserByUserId(String userId);
}
