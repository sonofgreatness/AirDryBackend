import Footer from "../../layouts/tpl/Footer";

export default function Manifesto() {
  return (
    <div className="page-padd manifesto-page">
      <div className="fade-in">

        {/* HERO */}
        <section className="manifesto-hero">
          <h1 className="manifest-title">
            🌲 Brotherhood of Timber Workers
          </h1>
          <p className="manifest-hero-sub">
            A manifesto for dignity, transparency, and shared progress
          </p>
        </section>

        {/* OPENING STATEMENT */}
        <section className="manifesto-block">
          <p className="manifesto-lead">
            We are the hands that shape the forest.<br />
            The heartbeat of every sawmill.<br />
            The quiet rhythm behind every beam, plank, and frame that builds our world.
          </p>
        </section>

        {/* PROBLEM */}
        <section className="manifesto-block manifesto-muted">
         <p>
  For years, the work of timber men and women has sustained the industry.<br />
  Records were kept, payments calculated, and performance assessed — often beyond the worker’s direct view.<br />
  What was missing was not effort, but transparency.
</p>

          <p>
            We believe those who give their strength to the craft deserve
            <strong> clarity</strong>, <strong>respect</strong>, and a
            <strong> voice in their own story</strong>.
          </p>
        </section>

        {/* ORIGIN */}
        <section className="manifesto-block manifesto-highlight">
          <h3>Why We Exist</h3>
          <p>
            <strong>The Brotherhood of Timber Workers</strong> was born from belief —
            not protest, but progress.
          </p>

          <p>
            We use technology to bring fairness into the open.<br />
            To make work visible. To make value transparent.
          </p>

          <p>
            So every worker can see what they’ve earned — every day.<br />
            And every company can build trust that endures.
          </p>
        </section>

        {/* VALUES */}
        <section className="manifesto-values">
          <div className="manifesto-card">
            <span>🤝</span>
            <h4>Solidarity</h4>
            <p>Not hostility — shared ground between labor and leadership.</p>
          </div>

          <div className="manifesto-card">
            <span>🛠️</span>
            <h4>Constructive Discontent</h4>
            <p>The courage to improve what can be better — without bitterness.</p>
          </div>

          <div className="manifesto-card">
            <span>📊</span>
            <h4>Professional Competence</h4>
            <p>Because skill, integrity, and data move industries forward.</p>
          </div>
        </section>

        {/* CLOSING */}
        <section className="manifesto-oath">
          <p>
            This is our brotherhood.<br />
            A shared path between those who labor and those who lead.
          </p>

          <p>
            Together, we will make the timber trade
            <strong> stronger</strong>, <strong> fairer</strong>,
            and <strong> more human</strong>.
          </p>

          <h2 className="manifest-tail">
            Brotherhood of Timber Workers
          </h2>

          <h4 className="manifest-foot">
            Technology for transparency. Unity for progress.
          </h4>
        </section>

      </div>

      <Footer />
    </div>
  );
}
