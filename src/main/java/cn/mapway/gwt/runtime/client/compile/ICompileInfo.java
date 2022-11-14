package cn.mapway.gwt.runtime.client.compile;

import java.util.Date;

/**
 * CompileInfo
 *
 * @author zhang
 */
public interface ICompileInfo {
    Date getCompileTime();

    String getGitCommit();

    String getGitAuthor();
    String getVersion();
    Date getGitTime();
}
