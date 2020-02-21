    <tr>
      <td class="name" required>name</td>
      <td class="desc">Name of the column.</td>
    </tr>
    <tr>
      <td class="name">type</td>
      <td class="desc">Data type of the column. Its value has to be one of the <a href="../../javadoc/liquibase/change/core/LoadDataChange.LOAD_DATA_TYPE.html" >LOAD_DATA_TYPE</a></td>
    </tr>
    <tr>
      <td class="name">header</td>
      <td class="desc">Name of the column in the CSV file from which the value for the column shall be taken if it&apos;s different from
       the column name.<div class='note'><b>Note:</b> Ignored if <i>index</i> is also defined.</div>
      </td>
    </tr>
    <tr>
      <td class="name">index</td>
      <td class="desc"><span class='type'>integer</span> Index of the column in the CSV file from which the value for the column shall be taken</td>
    </tr>
