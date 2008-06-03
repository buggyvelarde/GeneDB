<%@attribute name="id" required="false"%>
<%@attribute name="className" required="false"%>

<div class="outer ${className}" id="${id}">
    <div class="middle">
        <b class="round">
          <b class="round1"><b class="roundborder"></b></b>
          <b class="round2"><b class="roundborder"></b></b>
          <b class="round3"><b class="roundbg"></b></b>
          <b class="round4"><b class="roundbg"></b></b>
          <b class="round5"><b class="roundbg"></b></b>
        </b>
        <div class="inner">
            <jsp:doBody/>
        </div>
        <b class="round">
          <b class="round5"><b class="roundbg"></b></b>
          <b class="round4"><b class="roundbg"></b></b>
          <b class="round3"><b class="roundbg"></b></b>
          <b class="round2"><b class="roundborder"></b></b>
          <b class="round1"><b class="roundborder"></b></b>
        </b>
    </div>
</div>