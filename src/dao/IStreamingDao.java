package dao;

import java.sql.SQLException;

public interface IStreamingDao {

	void stream(final DaoCallSupport daoCallSupport) throws SQLException;

}
