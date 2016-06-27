package io.escriba;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.util.Date;

import static io.escriba.Data.Status.Creating;

public class Data {

	public static final Serializer<Data> SERIALIZER = new Serializer<Data>() {

		@Override
		public int compare(Data first, Data second) {
			if (first.path != null && second.path != null) {
				return first.path.compareTo(second.path);
			} else if (first.path != null)
				return -1;
			else if (second.path != null)
				return 1;
			else
				return 0;
		}

		@Override
		public Data deserialize(@NotNull DataInput2 input, int available) throws IOException {
			Date createDate = new Date(input.readLong());
			Date updateDate = new Date(input.readLong());
			Date accessDate = new Date(input.readLong());
			Status status = Status.valueOf((int) input.readByte());
			String path = input.readUTF();

			return new Data(accessDate, createDate, path, status, updateDate);
		}

		@Override
		public void serialize(@NotNull DataOutput2 out, @NotNull Data value) throws IOException {
			out.writeLong(value.createDate.getTime());
			out.writeLong(value.updateDate.getTime());
			out.writeLong(value.accessDate.getTime());
			out.writeByte(value.status.value);
			out.writeUTF(value.path);
		}
	};

	public enum Status {
		Creating(10), Ok(20), Updating(30), Deleting(40);

		private final int value;

		Status(int value) {
			this.value = value;
		}

		public static Status valueOf(int value) {
			for (Status status : values())
				if (status.value == value)
					return status;

			return null;
		}
	}

	public final Date accessDate;
	public final Date createDate;
	public final String path;
	public final Status status;
	public final Date updateDate;

	public Data(Date accessDate, Date createDate, String path, Status status, Date updateDate) {
		this.accessDate = accessDate;
		this.createDate = createDate;
		this.path = path;
		this.status = status;
		this.updateDate = updateDate;
	}

	public Data() {
		accessDate = createDate = updateDate = new Date();
		path = null;
		status = Creating;
	}

	public Data(String path) {
		accessDate = createDate = updateDate = new Date();
		this.path = path;
		status = Creating;
	}

	public Data path(String path) {
		return new Data(accessDate, createDate, path, status, updateDate);
	}

	public Data status(Status status) {
		return new Data(accessDate, createDate, path, status, updateDate);
	}
}
