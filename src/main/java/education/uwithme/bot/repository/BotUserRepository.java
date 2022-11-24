package education.uwithme.bot.repository;

import education.uwithme.bot.entity.BotUser;
import org.springframework.data.repository.CrudRepository;

public interface BotUserRepository extends CrudRepository<BotUser, Long> {
}
