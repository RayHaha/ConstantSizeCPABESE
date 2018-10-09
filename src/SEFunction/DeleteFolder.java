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
				delAllFile(path + "/" + tempList[i]);//���R���ɧ��ح�����
				delFolder(path + "/" + tempList[i]);//�A�R�����ɧ�
				flag = true;
			}
		}
		return flag;
	}
		

	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); //�R�����ح��Ҧ����e
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); //�R�����ɧ�
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
