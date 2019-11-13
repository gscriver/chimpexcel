package com.github.bananaj.chimpexcel;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.github.alexanderwe.bananaj.connection.MailChimpConnection;
import com.github.alexanderwe.bananaj.model.list.MailChimpList;
import com.github.alexanderwe.bananaj.model.list.member.Member;
import com.github.alexanderwe.bananaj.utils.DateConverter;

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
	 * Export information for up to 10 Mailchimp audiences to workbook file (XLS).
	 * @throws Exception
	 */
	public static void writeAllListToExcel(MailChimpConnection chimpCon, String filepath, boolean show_merge) throws Exception {
		WritableWorkbook workbook = Workbook.createWorkbook(new File(filepath+".xls"));
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, false);
		WritableCellFormat times16format = new WritableCellFormat (times16font);

		List<MailChimpList> mailChimpLists = chimpCon.getLists(10,0);
		int index  = 0;
		for(MailChimpList mailChimpList : mailChimpLists){
			WritableSheet sheet = workbook.createSheet(mailChimpList.getName(), index);

			Label memberIDLabel = new Label(0, 0, "MemberID",times16format);
			Label email_addressLabel = new Label(1,0,"Email Address",times16format);
			Label timestamp_sign_inLabel = new Label(2,0,"Sign up",times16format);
			Label ip_signinLabel = new Label(3,0,"IP Sign up", times16format);
			Label timestamp_opt_inLabel = new Label(4,0,"Opt in",times16format);
			Label ip_optLabel = new Label(5,0,"IP Opt in", times16format);
			Label statusLabel = new Label(6,0,"Status",times16format);
			Label avg_open_rateLabel = new Label(7,0,"Avg. open rate",times16format);
			Label avg_click_rateLabel = new Label(8,0,"Avg. click rate",times16format);


			sheet.addCell(memberIDLabel);
			sheet.addCell(email_addressLabel);
			sheet.addCell(timestamp_sign_inLabel);
			sheet.addCell(ip_signinLabel);
			sheet.addCell(timestamp_opt_inLabel);
			sheet.addCell(ip_optLabel);
			sheet.addCell(statusLabel);
			sheet.addCell(avg_open_rateLabel);
			sheet.addCell(avg_click_rateLabel);

			List<Member> members = mailChimpList.getMembers(0,0);
			int merge_field_count = 0;

			if (show_merge){
				int last_column = 9;

				Iterator<Entry<String, Object>> iter = members.get(0).getMergeFields().entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, Object> pair = iter.next();
					sheet.addCell(new Label(last_column,0,pair.getKey(),times16format));
					iter.remove(); // avoids a ConcurrentModificationException
					last_column++;
					merge_field_count++;
				}
			}


			for(int i = 0 ; i < members.size();i++)
			{
				Member member = members.get(i);
				sheet.addCell(new Label(0,i+1,member.getId()));
				sheet.addCell(new Label(1,i+1,member.getEmailAddress()));
				sheet.addCell(new Label(2,i+1,member.getTimestampSignup() != null ? DateConverter.toISO8601UTC(member.getTimestampSignup()) : ""));
				sheet.addCell(new Label(3,i+1,member.getIpSignup() != null ? member.getIpSignup() : ""));
				sheet.addCell(new Label(4,i+1,member.getTimestampOpt() != null ? DateConverter.toISO8601UTC(member.getTimestampOpt()) : ""));
				sheet.addCell(new Label(5,i+1,member.getIpOpt() != null ? member.getIpOpt() : ""));
				sheet.addCell(new Label(6,i+1,member.getStatus().toString()));
				sheet.addCell(new Number(7,i+1,member.getStats().getAvgOpenRate()));
				sheet.addCell(new Number(8,i+1,member.getStats().getAvgClickRate()));

				if (show_merge){
					//add merge fields values
					int last_index = 9;
					Iterator<Entry<String, Object>> iter_member = member.getMergeFields().entrySet().iterator();
					while (iter_member.hasNext()) {
						Entry<String, Object> pair = iter_member.next();
						sheet.addCell(new Label(last_index,i+1,pair.getValue().toString()));
						iter_member.remove(); // avoids a ConcurrentModificationException
						last_index++;

					}
				}
			}

			CellView cell;

			int column_count = 9 + merge_field_count;
			for(int x=0;x<column_count;x++)
			{
				cell=sheet.getColumnView(x);
				cell.setAutosize(true);
				sheet.setColumnView(x, cell);
			}
			index++;
		}
		workbook.write();
		workbook.close();
		System.out.println("Writing to excel - done");
	}
}
