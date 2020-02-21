    <tr>
      <td class="name" required>name</td>
      <td class="desc">Name of the column to set</td>
    </tr>
    <tr>
      <td class="name">value</td>
      <td class="desc">New value of the column.
      <div class='note'><b>Note:</b> If not set the first <code>valueXXX</code> defined is used in the order they
       appear.</div></td>
    </tr>
    <tr>
      <td class="name">valueNumeric</td>
      <td class="desc"><span class='type'>integer</span> value of the column.</td>
    </tr>
    <tr>
      <td class="name">valueBoolean</td>
      <td class="desc"><span class='type'>boolean</span> value of the column.</td>
    </tr>
    <tr>
      <td class="name">valueDate</td>
      <td class="desc">Date and/or Time value to set the column to. The value is specified in one of the following forms: "YYYY-MM-DD", "hh:mm:ss" or "YYYY-MM-DDThh:mm:ss".</td>
    </tr>
    <tr>
      <td class="name">valueComputed</td>
      <td class="desc">A value that is returned from a function or procedure call. This attribute shall contain the function name to call.</td>
    </tr>
    <tr>
      <td class="name">valueBlobFile</td>
      <td class="desc">Path to a file, whose contents will be written as a BLOB (i.e. chunk of binary data).
      Must be either absolute or relative to the Change Log file location (absolute paths are, e.g.:
      <code>/usr/local/somefile.dat</code> on Unix or <code>c:\Directory\somefile.dat</code> on Windows. Please refer to
      <a href="http://docs.oracle.com/javase/7/docs/api/java/io/File.html">java.io.File javadoc</a>
      for the details of what to consider relative/absolute path).</td>
    </tr>
    <tr>
      <td class="name">valueClobFile</td>
      <td class="desc">Path to a file, whose contents will be written as a CLOB (i.e. chunk of character data). 
      Must be either absolute or relative to the Change Log file location (absolute paths are, e.g.:
      <code>/usr/local/somefile.dat</code>on Unix or <code>c:\Directory\somefile.dat</code> on Windows.
      Please refer to <a href="http://docs.oracle.com/javase/7/docs/api/java/io/File.html">java.io.File javadoc</a>
      for the details of what to consider relative/absolute path).</td>
    </tr>
    <tr>
      <td class="name">encoding</td>
      <td class="desc">Name of the encoding (as specified 
      <a href="http://docs.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html">in java.nio.Charset javadoc</a>)
       of the CLOB file (specified in <code>valueClobFile</code>) contents.
       <span class="default right">Default: <span class="val">'UTF-8'</span></span>
       <div class='note'><b>Note:</b> used only when <code>valueClobFile</code> attribute is specified, ignored otherwise.
       </div></td>
    </tr>