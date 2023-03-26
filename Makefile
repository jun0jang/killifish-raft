setup:
	curl -LO https://github.com/pinterest/ktlint/releases/download/0.50.0/ktlint
	chmod +x ./ktlint
	mv ./ktlint ./scripts/ktlint
	pre-commit install

lint:
	sh ./scripts/lint-correct.sh
