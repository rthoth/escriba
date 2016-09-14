package io.escriba;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.util.Date;

import static java.io.File.separator;
import static java.lang.Integer.toHexString;

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
			int dataDirIndex = input.readInt();

			return new DataEntry(create, access, update, mediaType, path, size, status, dataDirIndex);
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
			out.writeInt(entry.dataDirIndex);
		}
	};
	private static final int X = (int) 1e3;

	private static final int YX = (int) 1e6;

	public static DataEntry DEFAULT = new DataEntry();

	public enum Status {
		Creating(10), Ok(20), Updating(30), Deleting(40), Deleted(50);

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
	public final int dataDirIndex;
	public final String mediaType;
	public final String path;
	public final long size;
	public final Status status;
	public final Date update;

	public DataEntry(Date create, Date access, Date update, String mediaType, String path, long size, Status status, int dataDirIndex) {
		this.create = create;
		this.access = access;
		this.update = update;
		this.mediaType = mediaType;
		this.path = path;
		this.size = size;
		this.status = status;
		this.dataDirIndex = dataDirIndex;
	}

	public DataEntry() {
		create = access = update = new Date();
		mediaType = DEFAULT_MEDIA_TYPE;
		path = null;
		size = 0;
		status = Status.Creating;
		dataDirIndex = 0;
	}

	public Copy copy() {
		return new Copy(this);
	}

	public static String zyx(long id) {
		int z = (int) (id / YX);
		int y = (int) ((id % YX) / X);
		int x = (int) (id % X);
		return toHexString(z) + separator + toHexString(y) + separator + toHexString(x);
	}

	public static class Copy {
		private Date access;
		private Integer dataDirIndex;
		private String mediaType;
		private final DataEntry original;
		private String path;
		private Long size;
		private Status status;
		private Date update;

		public Copy(DataEntry original) {
			this.original = original;
		}

		public Copy access(Date access) {
			this.access = access;
			return this;
		}

		public Copy dataDirIndex(int dataDirIndex) {
			this.dataDirIndex = dataDirIndex;
			return this;
		}

		public DataEntry end() {
			Date create = original.create;

			Date access = this.access != null ? this.access : original.access;
			Date update = this.update != null ? this.update : original.update;
			String mediaType = this.mediaType != null ? this.mediaType : original.mediaType;
			long size = this.size != null ? this.size : original.size;
			Status status = this.status != null ? this.status : original.status;
			int dataDirIndex = this.dataDirIndex != null ? this.dataDirIndex : original.dataDirIndex;
			String path = this.path != null ? this.path : original.path;

			return new DataEntry(create, access, update, mediaType, path, size, status, dataDirIndex);
		}

		public Copy mediaType(String mediaType) {
			this.mediaType = mediaType;
			return this;
		}

		public Copy path(String path) {
			this.path = path;
			return this;
		}

		public Copy size(long size) {
			this.size = size;
			return this;
		}

		public Copy status(Status status) {
			this.status = status;
			return this;
		}

		public Copy update(Date update) {
			this.update = update;
			return this;
		}
	}
}
