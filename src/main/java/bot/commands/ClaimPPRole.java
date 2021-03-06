package bot.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bot.api.ScoreSaber;
import bot.db.DatabaseManager;
import bot.dto.SongScore;
import bot.dto.player.Player;
import bot.main.BotConstants;
import bot.utils.Format;
import bot.utils.ListValueUtils;
import bot.utils.Messages;
import bot.utils.RoleManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ClaimPPRole {
	public static void validateAndAssignRole(Member member, DatabaseManager db, ScoreSaber ss, MessageChannel channel, boolean showMessages) {

		long discordUserId = member.getUser().getIdLong();
		long playerId = Long.parseLong(db.getPlayerByDiscordId(discordUserId).getPlayerId());
		if (playerId == -1) {
			Messages.sendMessage("You are not registered. Use \"ru help\".", channel);
			return;
		}

		List<SongScore> topScores = ss.getTopScoresByPlayerId(playerId);
		if (topScores != null && topScores.size() > 0) {
			float maxPP = topScores.get(0).getPp();
			int ppMilestone = ListValueUtils.findHighestSurpassedValue(maxPP, BotConstants.ppRoleMilestones);
			if (ppMilestone != -1) {
				String milestoneRoleName = Format.foaaRole(BotConstants.ppRoles[Arrays.asList(BotConstants.ppRoleMilestones).indexOf(ppMilestone)]);
				if (!RoleManager.memberHasRole(member, " PP", milestoneRoleName)) {
					RoleManager.removeMemberRolesByName(member, BotConstants.ppRoleSuffix);
					Role assignedRole = RoleManager.assignRole(member, milestoneRoleName);
					if (assignedRole != null && showMessages) {
						Messages.sendMessage(Format.bold("🎉" + member.getAsMention() + " Congrats, your PP role was updated! " + assignedRole.getAsMention()) + " 🎉", channel);
					}
				} else if (showMessages) {
					Messages.sendMessage(member.getAsMention() + " You already have the role, please stahp.", channel);
				}
			}
		} else {
			Messages.sendMessage(member.getAsMention() + " You do need to have at least 300pp for a pp role.", channel);
		}
	}

	public static void validateAndAssignRoleForAll(MessageReceivedEvent event, DatabaseManager db, ScoreSaber ss) {
		List<Player> storedPlayers = db.getAllStoredPlayers();
		for (Player player : storedPlayers) {
			Member member = event.getGuild().getMemberById(player.getDiscordUserId());
			if (member != null) {
				validateAndAssignRole(member, db, ss, event.getTextChannel(), false);
			}
			try {
				TimeUnit.MILLISECONDS.sleep(125);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Messages.sendMessage("Members have been updated. ✔️", event.getChannel());
	}
}
