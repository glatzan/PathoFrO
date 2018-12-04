package com.patho.main.model.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import org.hibernate.annotations.Immutable;

import lombok.Getter;
import lombok.Setter;

@NamedNativeQuery(
	    name = "AccountingData.findAllBetweenDates",
	    query = "With totals as "+ 
	    		"( "+
	    			"select  to_timestamp(tk.dateofreceipt/1000) as creationdate , piz, get_slide_count(s.slideid) as scount, "+ 
	    				"case when s.slideid ilike '%HE%' then true else false end as he, "+
	    				"case when s.slideid ilike '%HE%' and (sm.material in ('Bulbi', 'Exenteratio', 'Glaskörperaspirat') or (s.commentary = '') IS false or tk.commentary ilike '%Depigmentierung%' or tk.commentary ilike '%Entkalkung%' or tk.commentary ilike '%Handeinbettung%') then true else false end as he2, "+
	    				"case when s.slideid not ilike '%HE%' and sp.type not like 'IMMUN' then true else false end as pas, "+
	    				"case when sp.type like 'IMMUN' then true else false end as imu "+
	    				"FROM slide s "+
	    					"left join stainingprototype sp on s.slideprototype_id = sp.id "+
	    					"left join block bl on s.parent_id = bl.id "+
	    					"left join sample sm on bl.parent_id = sm.id "+
	    					"left join task tk on sm.parent_id = tk.id "+
	    					"left join patient pt on tk.parent_id = pt.id "+
	    		") "+    		             
	    		"SELECT row_number() OVER () AS id, date_trunc( 'day' ,creationdate)\\:\\:date date, piz, sum(scount) as totalcount, "+
					"sum(case when he and not he2 then scount else 0 end) as \"4800\" , "+
					"sum(case when he2 then scount else 0 end)  \"4802\", "+
					"sum(case when pas then scount else 0 end)  \"4815\", "+
					"sum(case when imu then scount else 0 end)  \"4815Imu\" "+
	            "FROM totals "+
					"where creationdate >= :fromDate and creationdate <= :toDate "+
					"group by 2,3 "+
					"order by 2,3; ",
	    resultSetMapping = "AccountingData"
	)
	@SqlResultSetMapping(
	    name = "AccountingData",
	    classes = @ConstructorResult(
	        targetClass = AccountingData.class,
	        columns = {
	        	@ColumnResult(name = "id", type=Long.class),
	            @ColumnResult(name = "date", type=String.class),
	            @ColumnResult(name = "piz", type=String.class ),
	            @ColumnResult(name = "totalcount", type=Integer.class),
	            @ColumnResult(name = "4800", type=Integer.class),
	            @ColumnResult(name = "4802", type=Integer.class),
	            @ColumnResult(name = "4815", type=Integer.class),
	            @ColumnResult(name = "4815Imu", type=Integer.class)
	        }
	    )
	)
@Entity
@Immutable
@Getter
@Setter
public class AccountingData {
	
	@Id
	private long id;
	private Date date;
	private int year;
	private int month;
	private String piz;
	private int totalCount;
	private int _4800;
	private int _4802;
	private int _4015;
	private int _4815Imu;
	
	public AccountingData(long id, String date, String piz, int totalCount, int _4800, int _4802, int _4015, int _4815Imu) {
		this.id = id;
		try {
			this.date = new SimpleDateFormat("yyyy-MM-dd").parse(date);
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(this.date);
			this.year = calendar.get(Calendar.YEAR);
			this.month = calendar.get(Calendar.MONTH);
		} catch (ParseException e) {
		}
		
		this.piz = piz;
		this._4800 = _4800;
		this._4802 = _4802;
		this._4015 = _4015;
		this._4815Imu = _4815Imu;
	}
}
