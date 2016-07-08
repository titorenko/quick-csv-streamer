package uk.elementarysoftware.quickcsv.parser;

import java.io.IOException;
import java.io.InputStream;

import uk.elementarysoftware.quickcsv.api.ByteArraySource;
import uk.elementarysoftware.quickcsv.utils.IOUtils;

class InputStreamToByteArraySourceAdapter implements ByteArraySource {

    private final InputStream is;
    private final BufferPool pool;
    
    public InputStreamToByteArraySourceAdapter(InputStream is, BufferPool pool) {
        this.is = is;
        this.pool = pool;
    }
  
    @Override
    public ByteArrayItem getNext() throws IOException {
        byte[] buffer = pool.getBuffer();
        int read = is.read(buffer);
        boolean isEndReached = read < buffer.length;
        if (isEndReached) IOUtils.closeQuietly(is);
        return new ByteArrayItem(buffer, Math.max(0, read), isEndReached);
    }
}
