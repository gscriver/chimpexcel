package com.github.bananaj.chimpexcel;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

import com.github.bananaj.connection.MailChimpConnection;
import com.github.bananaj.model.list.MailChimpList;
import com.github.bananaj.model.list.member.Member;
import com.github.bananaj.utils.DateConverter;

import jxl.CellView;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class App {

	public static void main( String[] args ) {
		System.out.println( "ChimpExcel" );
		
		if (args.length != 1) {
			System.out.println( "Usage: chimpexcel <Mailchimp_API_Key>" );
			System.exit(1);
		}

		try {
			MailChimpConnection chimpCon = new MailChimpConnection(args[0]);

			// Save all lists to Excel
			writeAllListToExcel(chimpCon, "MailChimpLists", true);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Export Mailchimp audiences to workbook file (XLS).
	 * @throws Exception
	 */
	public static void writeAllListToExcel(MailChimpConnection chimpCon, String filepath, boolean show_merge) throws Exception {
		WritableWorkbook workbook = Workbook.createWorkbook(new File(filepath+".xls"));
		WritableFont times12font = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false);
		WritableCellFormat times12format = new WritableCellFormat (times12font);

		int index  = 0;
		for(MailChimpList mailChimpList : chimpCon.getLists()){
			WritableSheet sheet = workbook.createSheet(mailChimpList.getName(), index);
			int hcolumn=0;
			sheet.addCell(new Label(hcolumn++, 0, "MemberID", times12format));
			sheet.addCell(new Label(hcolumn++, 0, "Email Address", times12format));
			sheet.addCell(new Label(hcolumn++, 0, "Sign up", times12format));
			sheet.addCell(new Label(hcolumn++, 0, "IP Sign up", times12format));
			sheet.addCell(new Label(hcolumn++, 0, "Opt in", times12format));
			sheet.addCell(new Label(hcolumn++, 0, "IP Opt in", times12format));
			sheet.addCell(new Label(hcolumn++, 0, "Status", times12format));
			sheet.addCell(new Label(hcolumn++, 0, "Avg. open rate", times12format));
			sheet.addCell(new Label(hcolumn++, 0, "Avg. click rate", times12format));

			int row = 0;
			for(Member member : mailChimpList.getMembers()) {
				if (show_merge && row == 0) {
					// add column headers
					Iterator<Entry<String, Object>> iter = member.getMergeFields().entrySet().iterator();
					while (iter.hasNext()) {
						sheet.addCell(new Label(hcolumn++, 0, iter.next().getKey(), times12format));
					}
				}
				
				row++;
				int rcolumn=0;
				sheet.addCell(new Label(rcolumn++,row,member.getId()));
				sheet.addCell(new Label(rcolumn++,row,member.getEmailAddress()));
				sheet.addCell(new Label(rcolumn++,row,member.getTimestampSignup() != null ? DateConverter.toISO8601UTC(member.getTimestampSignup()) : ""));
				sheet.addCell(new Label(rcolumn++,row,member.getIpSignup() != null ? member.getIpSignup() : ""));
				sheet.addCell(new Label(rcolumn++,row,member.getTimestampOpt() != null ? DateConverter.toISO8601UTC(member.getTimestampOpt()) : ""));
				sheet.addCell(new Label(rcolumn++,row,member.getIpOpt() != null ? member.getIpOpt() : ""));
				sheet.addCell(new Label(rcolumn++,row,member.getStatus().toString()));
				sheet.addCell(new Number(rcolumn++,row,member.getStats().getAvgOpenRate()));
				sheet.addCell(new Number(rcolumn++,row,member.getStats().getAvgClickRate()));

				if (show_merge) {
					//add merge fields values
					Iterator<Entry<String, Object>> iter_member = member.getMergeFields().entrySet().iterator();
					while (iter_member.hasNext()) {
						Entry<String, Object> pair = iter_member.next();
						sheet.addCell(new Label(rcolumn++,row,pair.getValue().toString()));
					}
				}
			}
			
			CellView cell;

			for(int i=0; i<hcolumn; i++)
			{
				cell=sheet.getColumnView(i);
				cell.setAutosize(true);
				sheet.setColumnView(i, cell);
			}
			index++;
		}
		workbook.write();
		workbook.close();
		System.out.println("Writing to excel - done");
	}
}
