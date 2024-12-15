package com.booreg.bingo.model;

public class Settings
{
	private int numberOfRows            ;
	private int numberOfColumns         ;
	private int numberOfCards           ;
	private int numberOfEmptyCellsPerRow;
	
	private int rowHeight  ;
	private int columnWidth;

	public int getNumberOfRows() {		return numberOfRows;	}
	public int getNumberOfColumns() {		return numberOfColumns;	}
	public int getNumberOfCards() {		return numberOfCards;	}
	public int getNumberOfEmptyCellsPerRow() {		return numberOfEmptyCellsPerRow;	}

	public int getRowHeight() {		return rowHeight;	}
	public void setRowHeight(int rowHeight) {		this.rowHeight = rowHeight;	}
	
	public void setNumberOfRows(int numberOfRows) {		this.numberOfRows = numberOfRows;	}
	public void setNumberOfColumns(int numberOfColumns) {		this.numberOfColumns = numberOfColumns;	}
	public void setNumberOfCards(int numberOfcards) {		this.numberOfCards = numberOfcards;	}
	public void setNumberOfEmptyCellsPerRow(int numberOfEmptyCellsPerRow) {		this.numberOfEmptyCellsPerRow = numberOfEmptyCellsPerRow;	}
	
	public int getColumnWidth() {		return columnWidth;	}
	public void setColumnWidth(int columnWidth) {		this.columnWidth = columnWidth;	}
}
