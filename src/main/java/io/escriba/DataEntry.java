package io.escriba;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.util.Date;

import static java.io.File.separator;

public class DataEntry {

	public static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
	public static final Serializer<DataEntry> SERIALIZER = new Serializer<DataEntry>() {

		@Override
		public int compare(DataEntry first, DataEntry second) {
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
		public DataEntry deserialize(@NotNull DataInput2 input, int available) throws IOException {
			Date create = new Date(input.readLong());
			Date access = new Date(input.readLong());
			Date update = new Date(input.readLong());
			String mediaType = input.readUTF();
			String path = input.readUTF();
			long size = input.readLong();
			Status status = Status.valueOf(input.readByte());

			return new DataEntry(create, access, update, mediaType, path, size, status);
		}

		@Override
		public void serialize(@NotNull DataOutput2 out, @NotNull DataEntry entry) throws IOException {
			out.writeLong(entry.create.getTime());
			out.writeLong(entry.access.getTime());
			out.writeLong(entry.update.getTime());
			out.writeUTF(entry.mediaType);
			out.writeUTF(entry.path);
			out.writeLong(entry.size);
			out.writeByte(entry.status.value);
		}
	};
	private static final int X = (int) 1e3;

	private static final int YX = (int) 1e6;

	public enum Status {
		Creating(10), Ok(20), Updating(30), Deleting(40);

		private final int value;

		Status(int value) {
			this.value = value;
		}


		public static Status valueOf(int value) {
			for (Status status : Status.values())
				if (status.value == value)
					return status;

			return null;
		}
	}

	public final Date access;
	public final Date create;
	public final String mediaType;
	public final String path;
	public final long size;
	public final Status status;
	public final Date update;

	public DataEntry(Date create, Date access, Date update, String mediaType, String path, long size, Status status) {
		this.create = create;
		this.access = access;
		this.update = update;
		this.mediaType = mediaType;
		this.path = path;
		this.size = size;
		this.status = status;
	}

	public DataEntry() {
		create = access = update = new Date();
		mediaType = DEFAULT_MEDIA_TYPE;
		path = null;
		size = 0;
		status = Status.Creating;
	}

	public DataEntry mediaType(String mediaType) {
		return new DataEntry(create, access, update, mediaType, path, size, status);
	}

	public DataEntry path(String path) {
		return new DataEntry(create, access, update, mediaType, path, size, status);
	}

	public DataEntry size(long size) {
		return new DataEntry(create, access, update, mediaType, path, size, status);
	}

	public DataEntry status(Status status) {
		return new DataEntry(create, access, update, mediaType, path, size, status);
	}

	public static String zyx(long value) {
		int z = (int) (value / YX);
		int y = (int) ((value % YX) / X);
		int x = (int) (value % X);
		return new StringBuilder().append(z).append(separator).append(y).append(separator).append(x).toString();
	}
}
