(() => {
  const byId = (id) => document.getElementById(id);

  const afmInput = byId("afm");
  const amkaInput = byId("amka");
  const passInput = byId("rawPassword");

  const afmHint = byId("afmHint");
  const amkaHint = byId("amkaHint");

  const onlyDigits = (s) => (s || "").replace(/\D+/g, "");

  function setHint(el, msg, isOk) {
    if (!el) return;

    const hasMsg = Boolean(msg);
    el.textContent = msg || "";

    el.classList.toggle("mc-hint-empty", !hasMsg);
    el.classList.toggle("mc-hint-ok", hasMsg && Boolean(isOk));
    el.classList.toggle("mc-hint-error", hasMsg && !Boolean(isOk));
  }

  function validateDigits(inputEl, hintEl, requiredLen, label) {
    if (!inputEl) return false;

    const digits = onlyDigits(inputEl.value);
    if (digits !== inputEl.value) inputEl.value = digits;

    if (digits.length === 0) {
      setHint(hintEl, "", true);
      return false;
    }

    if (digits.length < requiredLen) {
      setHint(
        hintEl,
        `${label}: λείπουν ${requiredLen - digits.length} ψηφία (πρέπει να είναι ${requiredLen}).`,
        false
      );
      return false;
    }

    if (digits.length > requiredLen) {
      setHint(
        hintEl,
        `${label}: έχεις βάλει ${digits.length - requiredLen} παραπάνω ψηφία (πρέπει να είναι ${requiredLen}).`,
        false
      );
      return false;
    }

    setHint(hintEl, `${label}: σωστός αριθμός ψηφίων.`, true);
    return true;
  }

  function toggleRule(ruleName, ok) {
    const item = document.querySelector(`.mc-pass-rule[data-rule="${ruleName}"]`);
    if (!item) return;
    item.classList.toggle("is-ok", Boolean(ok));
  }

  function validatePassword() {
    if (!passInput) return false;
    const v = passInput.value || "";

    const okLen = v.length >= 9;
    const okLetter = /\p{L}/u.test(v);
    const okSymbol = /[^\p{L}\d]/u.test(v);

    toggleRule("len", okLen);
    toggleRule("letter", okLetter);
    toggleRule("symbol", okSymbol);

    return okLen && okLetter && okSymbol;
  }

  function init() {
    if (afmInput) {
      afmInput.addEventListener("input", () => validateDigits(afmInput, afmHint, 9, "ΑΦΜ"));
      validateDigits(afmInput, afmHint, 9, "ΑΦΜ");
    }

    if (amkaInput) {
      amkaInput.addEventListener("input", () => validateDigits(amkaInput, amkaHint, 11, "ΑΜΚΑ"));
      validateDigits(amkaInput, amkaHint, 11, "ΑΜΚΑ");
    }

    if (passInput) {
      passInput.addEventListener("input", validatePassword);
      validatePassword();
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
