package dao.tz;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.CSVParser;
import util.IConsumer;
import dao.DAOParams;
import dao.DaoCallSupport;
import dao.IStreamingDao;
import domain.TimezoneInfo;

public class TimezoneDao implements IStreamingDao {
	
	public static final String PAGE_PARAMETER_NAME = "page";
	public static final String PAGE_SIZE_PARAMETER_NAME = "pageSize";
	public static final String ID_PARAMETER_NAME = "timezoneId";
	
	private static final Logger LOGGER = LogManager.getLogger(TimezoneDao.class);
	
	private static final List<TimezoneInfo> TZ_LIST;

	static {
		final ArrayList<TimezoneInfo> list = new ArrayList<TimezoneInfo>();
		try {
			new CSVParser("tz-abbr.csv", "UTF-8").parse(new String[3], new IConsumer<String[]>() {
				@Override
				public void accept(String[] tokens) {
					list.add(new TimezoneInfo(BigDecimal.valueOf(list.size()), tokens[0], tokens[1], tokens[2]));
				}
			});
		} catch (IOException e) {
			LOGGER.error("Failed to initialize", e);
		}
//		TZ_LIST = Collections.<TimezoneInfo> unmodifiableList(list);
		TZ_LIST = list;
	}
	
	public int getTimezoneInfoTotal() {
		return TZ_LIST.size();
	}

	public TimezoneInfo find(final DAOParams daoParams) {
		return this.findById((Integer) daoParams.getParameter(ID_PARAMETER_NAME));
	}
	
	public TimezoneInfo findById(final Integer id) {
		if(id!= null && id >= 0 && id < TZ_LIST.size()) {
			return TZ_LIST.get(id);
		}
		return TimezoneInfo.EMPTY;
	}
	
	public void update(final TimezoneInfo tzInfo) {
		if(tzInfo != null && tzInfo.getId() != null) {
			final int id = tzInfo.getId().intValue();
			if(id >= 0 && id < TZ_LIST.size()) {
				TZ_LIST.remove(id);
				TZ_LIST.add(id, tzInfo);
			}
		}
	}
	
	@Override
	public void stream(final DaoCallSupport daoCallSupport) throws SQLException {
		Integer pageSize = (Integer) daoCallSupport.getDaoParams().getParameter(PAGE_SIZE_PARAMETER_NAME);
		if(pageSize > 0 && TZ_LIST.size() > 0) {
			if(pageSize >= TZ_LIST.size()) {
				pageSize = TZ_LIST.size();
			}
			Integer page = (Integer) daoCallSupport.getDaoParams().getParameter(PAGE_PARAMETER_NAME);
			if(page < 1) {
				page = 1;
			}
			int startIndex = (page - 1) * pageSize;
			if(startIndex >= TZ_LIST.size()) {
				startIndex = TZ_LIST.size() / pageSize * pageSize;
			}
			int endIndex = startIndex + pageSize;
			if(endIndex >= TZ_LIST.size()) {
				endIndex = TZ_LIST.size() - 1;
			}
			final int bufferSize = 3;
			final List<TimezoneInfo> tzInfoList = new ArrayList<>(bufferSize);
			for(int i = startIndex; i <= endIndex;) {
				tzInfoList.clear();
				for(int j = 0; j < bufferSize && i <= endIndex; j++, i++) {
					tzInfoList.add(TZ_LIST.get(i));
				}
				daoCallSupport.getCallback().call(tzInfoList);
			}
		}
	}
}
