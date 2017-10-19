package dao.tz;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.trie.PatriciaTrie;
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
	public static final String ABBREVIATION_PARAM_NAME = "timezoneAbbr";	
	private static final Logger LOGGER = LogManager.getLogger(TimezoneDao.class);
	
	private static final List<TimezoneInfo> TZ_LIST = new ArrayList<TimezoneInfo>();
    private static final PatriciaTrie<TimezoneInfo> TZ_ABBR_TRIE = new PatriciaTrie<TimezoneInfo>();

	static {
		try {
			new CSVParser("tz-abbr.csv", "UTF-8").parse(new String[3], new IConsumer<String[]>() {
				@Override
				public void accept(String[] tokens) {
					final TimezoneInfo tzInfo = new TimezoneInfo(BigDecimal.valueOf(TZ_LIST.size()), tokens[0], tokens[1], tokens[2]);
					TZ_LIST.add(tzInfo);
					TZ_ABBR_TRIE.put(tokens[0], tzInfo);
				}
			});
		} catch (IOException e) {
			LOGGER.error("Failed to initialize", e);
		}
	}

	// synchronize updates
	private final Object lockHolder = new Object();
	
	public int getTimezoneInfoTotal() {
		synchronized(this.lockHolder) {
			return TZ_LIST.size();
		}
	}
	
	public int getTimezoneInfoTotal(final String abbrPrefix) {
		if(abbrPrefix == null) {
			return this.getTimezoneInfoTotal();
		}
		return this.findByAbbreviationPrefix(abbrPrefix.toUpperCase()).size();
	}

	public TimezoneInfo find(final DAOParams daoParams) {
		return this.findById((Integer) daoParams.getParameter(ID_PARAMETER_NAME));
	}
	
	public TimezoneInfo findById(final Integer id) {
		synchronized(this.lockHolder) {
			if(id!= null && id >= 0 && id < TZ_LIST.size()) {
				return TZ_LIST.get(id);
			}
			return TimezoneInfo.EMPTY;
		}
	}
	
	public Collection<TimezoneInfo> findByAbbreviationPrefix(final String abbrPrefix) {
		synchronized(this.lockHolder) {
			return TZ_ABBR_TRIE.prefixMap(abbrPrefix).values();
		}
	}
	
	public void update(final TimezoneInfo tzInfo) {
		if(tzInfo != null && tzInfo.getId() != null) {
			final int id = tzInfo.getId().intValue();
			synchronized(this.lockHolder) {
				if(id >= 0 && id < TZ_LIST.size()) {
					TZ_LIST.remove(id);
					TZ_LIST.add(id, tzInfo);
					TZ_ABBR_TRIE.put(tzInfo.getAbbreviation(), tzInfo);
				}
			}
		}
	}
	
	@Override
	public void stream(final DaoCallSupport daoCallSupport) throws SQLException {
		final List<TimezoneInfo> tzList = this.getList(daoCallSupport);
		Integer pageSize = (Integer) daoCallSupport.getDaoParams().getParameter(PAGE_SIZE_PARAMETER_NAME);
		if(pageSize > 0 && tzList.size() > 0) {
			if(pageSize >= tzList.size()) {
				pageSize = tzList.size();
			}
			Integer page = (Integer) daoCallSupport.getDaoParams().getParameter(PAGE_PARAMETER_NAME);
			if(page < 1) {
				page = 1;
			}
			int startIndex = (page - 1) * pageSize;
			if(startIndex >= tzList.size()) {
				startIndex = tzList.size() - pageSize;
			}
			int endIndex = startIndex + pageSize;
			if(endIndex >= tzList.size()) {
				endIndex = tzList.size() - 1;
			}
			final int bufferSize = 3;
			final List<TimezoneInfo> tzInfoList = new ArrayList<>(bufferSize);
			for(int i = startIndex; i <= endIndex;) {
				tzInfoList.clear();
				for(int j = 0; j < bufferSize && i <= endIndex; j++, i++) {
					tzInfoList.add(tzList.get(i));
				}
				daoCallSupport.getCallback().call(tzInfoList);
			}
		}
	}

	private List<TimezoneInfo> getList(final DaoCallSupport daoCallSupport) {
		final String abbrPrefix = (String)daoCallSupport.getDaoParams().getParameter(ABBREVIATION_PARAM_NAME);
		if(abbrPrefix != null) {
			return new ArrayList<TimezoneInfo>(this.findByAbbreviationPrefix(abbrPrefix.toUpperCase()));
		}
		synchronized(this.lockHolder) {
			return TZ_LIST;
		}
	}
}
