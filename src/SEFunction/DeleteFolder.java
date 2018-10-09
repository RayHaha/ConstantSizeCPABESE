package SEFunction;

import java.io.File;

public class DeleteFolder {
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);//先刪除檔夾裏面的檔
				delFolder(path + "/" + tempList[i]);//再刪除空檔夾
				flag = true;
			}
		}
		return flag;
	}
		

	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); //刪除完裏面所有內容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); //刪除空檔夾
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
