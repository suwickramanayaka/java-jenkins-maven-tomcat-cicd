package dev.cicddemo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/", "/health", "/version"}, loadOnStartup = 1)
public class ReleaseServlet extends HttpServlet {
    private ReleaseInfo release;

    @Override public void init() throws ServletException {
        try { release = ReleaseInfo.load(); }
        catch (IOException | IllegalArgumentException error) { throw new ServletException(error); }
    }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; frame-ancestors 'none'");
        switch (request.getServletPath()) {
            case "/health" -> {
                response.setContentType("text/plain");
                response.getWriter().println("UP");
            }
            case "/version" -> {
                response.setContentType("text/plain");
                response.getWriter().println(release.commit());
            }
            case "/", "" -> {
                response.setContentType("text/html");
                response.getWriter().print(render(release));
            }
            default -> response.sendError(404);
        }
    }

    public static String render(ReleaseInfo info) {
        return """
            <!doctype html>
            <html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
            <title>%s · Release Observatory</title>
            <style>
            :root{color-scheme:dark;--ink:#f3f4ee;--muted:#a3afa9;--line:#303c37;--green:#c0ed95}
            *{box-sizing:border-box}body{margin:0;background:#111a16;color:var(--ink);font:16px/1.65 system-ui,sans-serif}
            .wrap{max-width:1100px;margin:auto;padding:32px 28px}nav{display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid var(--line);padding-bottom:24px}
            .brand{font-weight:750;letter-spacing:-.5px}.mark{color:var(--green);margin-right:10px}.badge{font-size:12px;letter-spacing:1px;color:var(--green);border:1px solid #486043;padding:6px 12px;border-radius:30px}
            main{padding:65px 0 40px}.eyebrow{font:12px monospace;text-transform:uppercase;letter-spacing:2px;color:var(--green)}h1{font-size:clamp(40px,6vw,72px);line-height:1.08;letter-spacing:-3px;max-width:800px;margin:20px 0 24px}
            .intro{max-width:620px;color:var(--muted);font-size:18px}.release{margin-top:42px;border:1px solid var(--line);border-radius:16px;overflow:hidden;background:#18231d}
            .release-head{display:flex;justify-content:space-between;gap:15px;align-items:center;padding:22px 26px;border-bottom:1px solid var(--line)}h2{font-size:18px;margin:0}.online{font-size:13px;color:var(--green)}
            .facts{display:grid;grid-template-columns:repeat(3,1fr)}.fact{padding:24px 26px;min-width:0}.fact+.fact{border-left:1px solid var(--line)}.label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:1px}.value{font:23px monospace;margin-top:8px;overflow-wrap:anywhere}
            .flow{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-top:30px}.step{padding:18px;border-top:1px solid #536449}.num{font:12px monospace;color:var(--green)}.step strong{display:block;margin:10px 0 4px}.step p{color:var(--muted);font-size:13px;margin:0}
            footer{border-top:1px solid var(--line);padding-top:22px;margin-top:30px;color:var(--muted);font-size:12px;display:flex;justify-content:space-between;gap:15px}a{color:var(--green)}
            @media(max-width:600px){.wrap{padding:22px}main{padding-top:40px}h1{letter-spacing:-1.5px}.facts,.flow{grid-template-columns:1fr}.fact+.fact{border-left:0;border-top:1px solid var(--line)}.release-head,footer{align-items:flex-start;flex-direction:column}}
            </style></head><body><div class="wrap">
            <nav><div class="brand"><span class="mark">◈</span>Release Observatory</div><span class="badge">JAVA / CI-CD</span></nav>
            <main><div class="eyebrow">A small application. A complete delivery journey.</div><h1>%s</h1>
            <p class="intro">Every release connects source code to a running Java application. This page identifies the artifact currently served by Apache Tomcat.</p>
            <section class="release" aria-label="Current release"><div class="release-head"><h2>Current release</h2><span class="online">● Application running</span></div>
            <div class="facts"><div class="fact"><div class="label">Application version</div><div class="value">%s</div></div>
            <div class="fact"><div class="label">Source commit</div><div class="value">%s</div></div>
            <div class="fact"><div class="label">Jenkins build</div><div class="value">%s</div></div></div></section>
            <div class="flow"><div class="step"><span class="num">01 / SOURCE</span><strong>Git</strong><p>A versioned change starts the journey.</p></div>
            <div class="step"><span class="num">02 / AUTOMATE</span><strong>Jenkins</strong><p>The pipeline coordinates delivery.</p></div>
            <div class="step"><span class="num">03 / PACKAGE</span><strong>Maven</strong><p>Java code becomes a tested WAR.</p></div>
            <div class="step"><span class="num">04 / SERVE</span><strong>Tomcat</strong><p>The release becomes accessible.</p></div></div></main>
            <footer><span>This page is developed by Sithum Wickramanayaka.</span><span><a href="health">Health</a> &nbsp; / &nbsp; <a href="version">Full commit</a></span></footer>
            </div></body></html>
            """.formatted(ReleaseInfo.escapeHtml(info.name()), ReleaseInfo.escapeHtml(info.message()),
                ReleaseInfo.escapeHtml(info.version()), ReleaseInfo.escapeHtml(info.shortCommit()), ReleaseInfo.escapeHtml(info.build()));
    }
}
