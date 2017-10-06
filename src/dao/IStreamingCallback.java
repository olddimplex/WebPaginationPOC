package dao;

import java.sql.SQLException;

public interface IStreamingCallback {

	void call(final Object dataObject) throws SQLException;
}
