package com.booreg.bingo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import com.booreg.bingo.model.Card;
import com.booreg.bingo.model.Settings;

public class Bingo
{
	private static final String SONG_KEY = "SONG";
	
	private int getRandomInt(int min, int max)
	{
		int result = ThreadLocalRandom.current().nextInt(min, max);
		
		return result;
	}
	
	private Collection<Integer> generateEmptyCells(int emptyCellsPerRow, int maxCellNumber)
	{
		Collection<Integer> result = new TreeSet<>();
		
		while (result.size() < emptyCellsPerRow)
		{
			int randomCell = getRandomInt(0, maxCellNumber);
			
			if (!result.contains(randomCell))
			{
				result.add(randomCell);
			}
		}
		
		return result;
	}

	private Sheet autoAdjustColumns(Sheet sheet, int additionalWidth)
	{
		Sheet result = sheet;
		
		int firstRow = result.getFirstRowNum();
		Row row = result.getRow(firstRow);
		int firstCell = row.getFirstCellNum();
		int lastCell  = row.getLastCellNum();

		for (int i = firstCell; i <= lastCell; i++)
		{
			result.autoSizeColumn(i);
			result.setColumnWidth(i, result.getColumnWidth(i) + additionalWidth);
		}
		
		return result;
	}

	private Font getDefaultFont(Workbook workbook)
	{
		Font result = workbook.createFont();
		
		result.setFontName("Verdana");
		result.setFontHeightInPoints((short) 16);
		
		return result;
	}

	private CellStyle getDefaultCellStyle(Workbook workbook, Font font)
	{
		CellStyle result = workbook.createCellStyle();
		result.setFont(font);
		
		return result;
	}

	private CellStyle getCardCellStyle(Workbook workbook, Font font)
	{
		CellStyle result = getDefaultCellStyle(workbook, font);
		
		result.setAlignment(HorizontalAlignment.CENTER);
		result.setVerticalAlignment(VerticalAlignment.CENTER);
		result.setWrapText(true);
		result.setShrinkToFit(true);
		
		result.setBorderTop   (BorderStyle.MEDIUM);
		result.setBorderRight (BorderStyle.MEDIUM);
		result.setBorderBottom(BorderStyle.MEDIUM);
		result.setBorderLeft  (BorderStyle.MEDIUM);
		
		return result;
	}

	private CellStyle getSongCellStyle(Workbook workbook, Font font)
	{
		CellStyle result = getCardCellStyle(workbook, font);
		
		return result;
	}

	private CellStyle getEmptyCellStyle(Workbook workbook, Font font)
	{
		CellStyle result = getCardCellStyle(workbook, font);

		result.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
		result.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		result.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		return result;
	}
	
	public Collection<Card> generateCards(Settings settings) throws IOException
	{
		Properties sourceSongs = new Properties();
		
		Collection<Card> result = new ArrayList<>();
		
		try(InputStream inputStream = getClass().getResourceAsStream("songs.properties"))
		{
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			sourceSongs.load(inputStreamReader);
			
			int emptyCellsPerRow = settings.getNumberOfEmptyCellsPerRow();
			int numberOfColumns  = settings.getNumberOfColumns();
			int numberOfSongs    = sourceSongs.size();
			
			for (int i = 0; i < settings.getNumberOfCards(); i++)
			{
				// Cards
				
				Card card = new Card();
				result.add(card);
				
				List<List<String>> songs = new ArrayList<>();
				card.setSongs(songs);
				
				Collection<Integer> cardSongs = new TreeSet<>();
				
				for (int rowIndex = 0; rowIndex < settings.getNumberOfRows(); rowIndex++)
				{
					// Rows
					
					List<String> row = new ArrayList<>();
					songs.add(row);

					Collection<Integer> emptyCells = generateEmptyCells(emptyCellsPerRow, numberOfColumns);

					for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++)
					{
						// Columns
						
						String song = null;
						
						if (!emptyCells.contains(columnIndex))
						{
							boolean usedSong = true;
							
							int randomSong = -1;
							
							while(usedSong)
							{
								randomSong = getRandomInt(1, numberOfSongs);
								usedSong = cardSongs.contains(randomSong);
							}
							
							cardSongs.add(randomSong);

							song = sourceSongs.getProperty(SONG_KEY + randomSong);
						}
						
						row.add(song);
					}
				}
			}
		}
		
		return result;
	}
	
	public void write(Settings settings, Collection<Card> cards, String filename) throws FileNotFoundException, IOException
	{
		File file = new File(filename);
		
		Files.deleteIfExists(file.toPath());
		
		try(FileOutputStream fileoutputStream = new FileOutputStream(file))
		{
			Workbook workbook = new SXSSFWorkbook(cards.size() * settings.getNumberOfRows());
			
			Font defaulFont = getDefaultFont(workbook);
			
			CellStyle defaultCellStyle = getDefaultCellStyle(workbook, defaulFont);
			CellStyle songCellStyle    = getSongCellStyle(workbook, defaulFont);
			CellStyle emptyCellStyle   = getEmptyCellStyle(workbook, defaulFont);
			
			Sheet sheet = workbook.createSheet();

			sheet.setDefaultRowHeight  ((short) (settings.getRowHeight() * 20));
			sheet.setDefaultColumnWidth(settings.getColumnWidth());
			
			int rowIndex = 0;
			int cardIndex = 1;
			
			for(Card card:cards)
			{
				// Card header
				
				Row preHeaderRow = sheet.createRow(rowIndex++);
				preHeaderRow.setHeight((short) (20 * 20));
				Row headerRow = sheet.createRow(rowIndex++);
				Cell cellHeader = headerRow.createCell(0, CellType.NUMERIC);
				cellHeader.setCellValue(Integer.toString(cardIndex++));
				cellHeader.setCellStyle(defaultCellStyle);
				Row postHeaderRow = sheet.createRow(rowIndex++);
				postHeaderRow.setHeight((short) (20 * 20));

				for (List<String> cardRow:card.getSongs())
				{
					Row row = sheet.createRow(rowIndex++);
					row.setHeight((short) (settings.getRowHeight() * 20));
					
					int cellIndex = 0;
					
					for (String song:cardRow)
					{
						CellType type = song != null ? CellType.STRING : CellType.BLANK;
						
						Cell cell = row.createCell(cellIndex++, type);

						if (song != null)
						{
							cell.setCellValue(song);
							cell.setCellStyle(songCellStyle);
						}
						else
						{
							cell.setCellStyle(emptyCellStyle);
						}
					}
				}
			}
			
	        workbook.write(fileoutputStream);
	        workbook.close();
		}
	}
	

	public static void main(String[] args) throws IOException
	{
		System.out.println("Starting at " + LocalDateTime.now());
		
		Bingo bingo = new Bingo();
		
		Settings settings = new Settings();
		
		settings.setNumberOfCards           (100);
		settings.setNumberOfRows            (3);
		settings.setNumberOfColumns         (4);
		settings.setNumberOfEmptyCellsPerRow(1);
		settings.setRowHeight               (100);
		settings.setColumnWidth             (14);
		
		Collection<Card> cards = bingo.generateCards(settings);
		bingo.write(settings, cards, "C:\\AppServerFiles\\Bingo.xlsx");

		System.out.println("Ending at " + LocalDateTime.now());
		
	}
}
