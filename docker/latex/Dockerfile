# Compilation LaTeX -> PDF isolee (le .tex peut contenir du texte issu du projet analyse).
FROM texlive/texlive:latest
RUN useradd -m -u 1000 latex || true
WORKDIR /doc
USER 1000:1000
ENTRYPOINT ["pdflatex", "-interaction=nonstopmode", "-halt-on-error", "-no-shell-escape"]
