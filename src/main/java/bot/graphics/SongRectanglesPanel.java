package bot.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JPanel;

import bot.dto.SongScore;
import bot.utils.FontUtils;
import bot.utils.GraphicsUtils;

public class SongRectanglesPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private List<SongScore> scores;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		GraphicsUtils.drawSongRectangles((Graphics2D) g, 5, 0, scores);
		this.setBackground(FontUtils.translucent());
	}

	public List<SongScore> getScores() {
		return scores;
	}

	public void setScores(List<SongScore> scores) {
		this.scores = scores;
	}
}